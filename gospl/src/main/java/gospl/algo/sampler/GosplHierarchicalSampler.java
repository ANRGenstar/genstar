package gospl.algo.sampler;

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

import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationValue;
import core.util.random.GenstarRandomUtils;
import core.util.random.roulette.RouletteWheelSelectionFactory;
import gospl.distribution.matrix.ASegmentedNDimensionalMatrix;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.distribution.matrix.coordinate.GosplCoordinate;
import gospl.distribution.util.GosplBasicDistribution;

/**
 * A Hierarchical sampler explores the variables in a given order to generate the individuals.  
 * 
 * @author Samuel Thiriot
 *
 */
public class GosplHierarchicalSampler implements IHierarchicalSampler {

	private Logger logger = LogManager.getLogger();
	@SuppressWarnings("unused")
	private GosplBasicDistribution gosplBasicDistribution = null;
	private Collection<List<APopulationAttribute>> explorationOrder = null;
	private ASegmentedNDimensionalMatrix<Double> segmentedMatrix;
	
	public GosplHierarchicalSampler() {
		// TODO Auto-generated constructor stub
	}


	// -------------------- setup methods -------------------- //




	@Override
	public void setDistribution(
			GosplBasicDistribution gosplBasicDistribution,
			Collection<List<APopulationAttribute>> explorationOrder,
			ASegmentedNDimensionalMatrix<Double> segmentedMatrix
			) {
		this.gosplBasicDistribution = gosplBasicDistribution;
		this.explorationOrder = explorationOrder;
		this.segmentedMatrix = segmentedMatrix;
		
	}

	
	// -------------------- main contract -------------------- //

	@Override
	public ACoordinate<APopulationAttribute, APopulationValue> draw() {

		Map<APopulationAttribute,APopulationValue> att2value = new HashMap<>();
		
		logger.info("starting hierarchical sampling...");
		for (List<APopulationAttribute> subgraph : explorationOrder) {
			logger.info("starting hierarchical sampling for the first subgraph {}", subgraph);
			for (APopulationAttribute att: subgraph) {
			
				// maybe we processed it already ? (because of control attributes / mapped aspects)
				if (att2value.containsKey(att))
					continue;
				
				logger.info("\tsampling att {}", att);
			
				
				if (att2value.containsKey(att.getReferentAttribute())) {
					// this attribute as for a control attribute an attribute which was already sampled. 
					// we should probably not sample it, but rather use this reference with the user rules
					// so we can translate it. 
					logger.debug("\t{} was already defined to {}; let's reuse the mapping...", att.getReferentAttribute().getAttributeName(), att2value.get(att.getReferentAttribute()));
					
					APopulationValue referentValue = att2value.get(att.getReferentAttribute()); 
					Set<APopulationValue> mappedValues = att.findMappedAttributeValues(referentValue);

					logger.debug("\t\t{} maps to {}", att.getReferentAttribute(), mappedValues);
					if (mappedValues.size() > 1) {
						logger.warn("\t\thypothesis of uniformity for {} => {}", referentValue, mappedValues);	
					}
					APopulationValue theOneMapped = GenstarRandomUtils.oneOf(mappedValues);
					att2value.put(att, theOneMapped);
					logger.info("\t\tpicked {} = {} (through referent attribute)", att, theOneMapped);

					
				} else {
					
					logger.info("\tshould pick one of the values {}", att.getValues());
	
					// what we want is the distribution of probabilities for each of these possible values of the current attribute...
					List<APopulationValue> keys = new ArrayList<>(att.getValues());
					// TODO knowing the previous ones ! 
					keys.addAll(att2value.values());
					
					// for each of the aspects of this attribute we're working on...
					List<Double> distribution = new ArrayList<>(att.getValues().size()+1);
					@SuppressWarnings("unused")
					List<APopulationValue> a = new ArrayList<>(att2value.values());
					for (APopulationValue val : att.getValues()) {
						// we want the probabilities conditions to all the previously defined values
						List<APopulationValue> aa =  new ArrayList<>(att2value.values());
						// ... and for this specific val
						aa.add(val);
						// TODO sometimes I've here a NUllpointerexception when one of the values if empty (typically Age3)
						try {
							logger.debug("\t\tfor aspects: {}, getVal returns {}", aa, this.segmentedMatrix.getVal(aa));
							distribution.add(this.segmentedMatrix.getVal(aa).getValue());
						} catch (NullPointerException e) {
							logger.warn("\t\tpotential value {} will be excluded from the distribution as it has no probability", val);
						}
					}
					APopulationValue theOne = null;
					if (distribution.isEmpty()) {
						// okay, the mix of variables probably includes some "empty"; let's assume the value is then empty as well...
						// TODO what to do here ?
						theOne = att.getEmptyValue();
						logger.warn("\t\tempty distribution; let's assume default value");
					} else {
						theOne = RouletteWheelSelectionFactory.getRouletteWheel(distribution,keys).drawObject();
					}
					logger.info("\t\tpicked {} = {}", att, theOne);
					att2value.put(att, theOne);
					
					// well, we defined a value... maybe its defining the value of another thing ?
					if (att.getReferentAttribute() != att) {
						// yes, it has a reference attribute !
						Set<APopulationValue> mappedValues = att.findMappedAttributeValues(theOne);
						logger.debug("\twe have a reference attribute {}, which maps to {}", att.getReferentAttribute(), mappedValues);
						if (mappedValues.size() > 1) {
							logger.warn("\t\thypothesis of uniformity for {} => {}", theOne, mappedValues);	
						}
						// let's randomly draw something there 
						// another random
						APopulationValue theOneMapped = GenstarRandomUtils.oneOf(mappedValues);
						att2value.put(att.getReferentAttribute(), theOneMapped);
						logger.info("\t\tpicked {} = {} (through referent attribute)", att.getReferentAttribute(), theOneMapped);

					}
				}
			}
		}
		
		return new GosplCoordinate(new HashSet<>(att2value.values()));
		
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * WARNING: make use of {@link Stream#parallel()}
	 */
	@Override
	public final List<ACoordinate<APopulationAttribute, APopulationValue>> draw(int numberOfDraw) {
		return IntStream.range(0, numberOfDraw).parallel().mapToObj(i -> draw()).collect(Collectors.toList());
	}

	// -------------------- utility -------------------- //

	@Override
	public String toCsv(String csvSeparator) {
		// TODO Auto-generated method stub
		return null;
	}


}
