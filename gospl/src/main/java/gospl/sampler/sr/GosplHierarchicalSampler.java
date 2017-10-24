package gospl.sampler.sr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.metamodel.pop.attribute.DemographicAttribute;
import core.metamodel.value.IValue;
import core.util.random.GenstarRandomUtils;
import core.util.random.roulette.RouletteWheelSelectionFactory;
import gospl.distribution.matrix.ASegmentedNDimensionalMatrix;
import gospl.distribution.matrix.CachedSegmentedNDimensionalMatrix;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.distribution.matrix.ISegmentedNDimensionalMatrix;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.distribution.matrix.coordinate.GosplCoordinate;
import gospl.sampler.IHierarchicalSampler;

/**
 * A Hierarchical sampler explores the variables in a given order to generate the individuals.  
 * 
 * @author Samuel Thiriot
 *
 */
public class GosplHierarchicalSampler implements IHierarchicalSampler {

	private Logger logger = LogManager.getLogger();
	private Collection<List<DemographicAttribute<? extends IValue>>> explorationOrder = null;
	private ISegmentedNDimensionalMatrix<Double> segmentedMatrix;
	
	public GosplHierarchicalSampler() {
		// TODO Auto-generated constructor stub
	}


	// -------------------- setup methods -------------------- //




	@Override
	public void setDistribution(
			Collection<List<DemographicAttribute<? extends IValue>>> explorationOrder,
			ASegmentedNDimensionalMatrix<Double> segmentedMatrix
			) {
		this.explorationOrder = explorationOrder;
		
		// create a cached version of this segmented matrix, to save time in our intensive computation of probabilities
		this.segmentedMatrix = new CachedSegmentedNDimensionalMatrix<Double>(segmentedMatrix);
		
	}

	
	// -------------------- main contract -------------------- //

	@Override
	public ACoordinate<DemographicAttribute<? extends IValue>, IValue> draw() {

		Map<DemographicAttribute<? extends IValue>, IValue> att2value = new HashMap<>();
		
		logger.debug("starting hierarchical sampling...");
		for (List<DemographicAttribute<? extends IValue>> subgraph : explorationOrder) {
			logger.debug("starting hierarchical sampling for the first subgraph {}", subgraph);
			for (DemographicAttribute<? extends IValue> att: subgraph) {
			
				// maybe we processed it already ? (because of control attributes / mapped aspects)
				if (att2value.containsKey(att))
					continue;
				
				logger.debug("\tsampling att {}", att);
			
				if (att2value.containsKey(att.getReferentAttribute())) {
					// this attribute as for a control attribute an attribute which was already sampled. 
					// we should probably not sample it, but rather use this reference with the user rules
					// so we can translate it. 
					logger.debug("\t\t{} was already defined to {}; let's reuse the mapping...", 
							att.getReferentAttribute().getAttributeName(), att2value.get(att.getReferentAttribute()));
					
					IValue referentValue = att2value.get(att.getReferentAttribute()); 
					Set<? extends IValue> mappedValues = att.findMappedAttributeValues(referentValue);

					logger.trace("\t\t{} maps to {}", referentValue, mappedValues);
					if (mappedValues.size() > 1) {
						logger.warn("\t\thypothesis of uniformity for {} => {}", referentValue, mappedValues);	
					}
					IValue theOneMapped = GenstarRandomUtils.oneOf(mappedValues);
					att2value.put(att, theOneMapped);
					logger.debug("\t\tpicked {} = {} (through referent attribute)", att, theOneMapped);

					
				} else {
					
					logger.debug("\tshould pick one of the values {}", att.getValueSpace());
	
					// what we want is the distribution of probabilities for each of these possible values of the current attribute...
					List<IValue> keys = new ArrayList<>(att.getValueSpace());
					// TODO knowing the previous ones ! 
					keys.addAll(att2value.values());
					
					// for each of the aspects of this attribute we're working on...
					List<Double> distribution = new ArrayList<>(att.getValueSpace().size()+1);
					List<IValue> a = new ArrayList<>();
					// ... we want to add the values already defined that can condition the attribute of interest
					for (INDimensionalMatrix<DemographicAttribute<? extends IValue>, IValue, Double> m : this.segmentedMatrix
							.getMatricesInvolving(this.segmentedMatrix.getDimensions().stream()
									.filter(dim -> dim.equals(att.getReferentAttribute())).findAny().get())) {
						a.addAll(
								m.getDimensions()
									.stream()
									.filter(dim -> att2value.containsKey(dim))
									.map(dim -> att2value.get(dim))
									.collect(Collectors.toSet())
									);
					}
					double total = 0.;
					for (IValue val : att.getValueSpace()) {
						// construct the list of the attributes on which we want conditional probabilities
						Set<IValue> aa =  new HashSet<>(a);
						// att2value.values()
						
						//this.segmentedMatrix.getMatricesInvolving(val).stream().filter(matrix -> att2value.containsKey(key));
						// ... and for this specific val
						aa.add(val);
						// TODO sometimes I've here a NUllpointerexception when one of the values if empty (typically Age3)
						try {
							logger.trace("\t\tfor aspects: {}, getVal returns {}", aa, this.segmentedMatrix.getVal(aa));
							Double v = this.segmentedMatrix.getVal(aa).getValue();
							total += v;
							distribution.add(v);
						} catch (NullPointerException e) {
							logger.warn("\t\tpotential value {} will be excluded from the distribution as it has no probability", val);
						}
					}
					IValue theOne = null;
					if (distribution.isEmpty() || total==0.) {
						// okay, the mix of variables probably includes some "empty"; let's assume the value is then empty as well...
						// TODO what to do here ?
						theOne = att.getEmptyValue();
						logger.warn("\t\tempty distribution; let's assume default value");
					} else {
						theOne = RouletteWheelSelectionFactory.getRouletteWheel(distribution,keys).drawObject();
					}
					logger.debug("\t\tpicked {} = {}", att, theOne);
					att2value.put(att, theOne);
					
					// well, we defined a value... maybe its defining the value of another thing ?
					if (att.getReferentAttribute() != att) {
						// yes, it has a reference attribute !
						Set<? extends IValue> mappedValues = att.findMappedAttributeValues(theOne);
						logger.debug("\twe have a reference attribute {}, which maps to {}", att.getReferentAttribute(), mappedValues);
						if (mappedValues.size() > 1) {
							logger.warn("\t\thypothesis of uniformity for {} => {}", theOne, mappedValues);	
						}
						// let's randomly draw something there 
						// another random
						IValue theOneMapped = GenstarRandomUtils.oneOf(mappedValues);
						// Little trick to get referent attribute without <? extends IValue> wildcard
						att2value.put(this.segmentedMatrix.getDimensions().stream()
								.filter(dim -> dim.equals(att.getReferentAttribute())).findAny().get(), theOneMapped);
						logger.debug("\t\tpicked {} = {} (through referent attribute)", att.getReferentAttribute(), theOneMapped);

					}
				}
			}
		}
		
		// to test the efficiency of cache:
		/* 
		System.err.println(
				"hits / missed     "+((CachedSegmentedNDimensionalMatrix)this.segmentedMatrix).getHits()+"/"+
				((CachedSegmentedNDimensionalMatrix)this.segmentedMatrix).getMissed()
				);
		*/
		
		return new GosplCoordinate(att2value);
		
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * WARNING: make use of {@link Stream#parallel()}
	 */
	@Override
	public final List<ACoordinate<DemographicAttribute<? extends IValue>, IValue>> draw(int numberOfDraw) {
		return IntStream.range(0, numberOfDraw).parallel().mapToObj(i -> draw()).collect(Collectors.toList());
	}
	
	// -------------------- utility -------------------- //

	@Override
	public String toCsv(String csvSeparator) {
		// TODO Auto-generated method stub
		return null;
	}


}
