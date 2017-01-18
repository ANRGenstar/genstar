package gospl.algo.sampler.sr;

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

import core.metamodel.IPopulation;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;
import core.util.random.GenstarRandomUtils;
import core.util.random.roulette.RouletteWheelSelectionFactory;
import gospl.algo.evaluation.BasicSamplingEvaluation;
import gospl.algo.evaluation.IEvaluableSampler;
import gospl.algo.evaluation.ISamplingEvaluation;
import gospl.algo.evaluation.SamplingEvaluationUtils;
import gospl.algo.sampler.IHierarchicalSampler;
import gospl.distribution.GosplDistributionFactory;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.ASegmentedNDimensionalMatrix;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.distribution.matrix.coordinate.GosplCoordinate;

/**
 * A Hierarchical sampler explores the variables in a given order to generate the individuals.  
 * 
 * @author Samuel Thiriot
 *
 */
public class GosplHierarchicalSampler implements IHierarchicalSampler, IEvaluableSampler {

	private Logger logger = LogManager.getLogger();
	private Collection<List<APopulationAttribute>> explorationOrder = null;
	private ASegmentedNDimensionalMatrix<Double> segmentedMatrix;
	
	public GosplHierarchicalSampler() {
		// TODO Auto-generated constructor stub
	}


	// -------------------- setup methods -------------------- //




	@Override
	public void setDistribution(
			Collection<List<APopulationAttribute>> explorationOrder,
			ASegmentedNDimensionalMatrix<Double> segmentedMatrix
			) {
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
					logger.debug("\t\t{} was already defined to {}; let's reuse the mapping...", 
							att.getReferentAttribute().getAttributeName(), att2value.get(att.getReferentAttribute()));
					
					APopulationValue referentValue = att2value.get(att.getReferentAttribute()); 
					Set<APopulationValue> mappedValues = att.findMappedAttributeValues(referentValue);

					logger.debug("\t\t{} maps to {}", referentValue, mappedValues);
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
					List<APopulationValue> a = new ArrayList<>();
					// ... we want to add the values already defined that can condition the attribute of interest
					for (AFullNDimensionalMatrix<Double> m : this.segmentedMatrix.getMatricesInvolving(att.getReferentAttribute())) {
						a.addAll(
								m.getDimensions()
									.stream()
									.filter(dim -> att2value.containsKey(dim))
									.map(dim -> att2value.get(dim))
									.collect(Collectors.toSet())
									);
					}
					double total = 0.;
					for (APopulationValue val : att.getValues()) {
						// construct the list of the attributes on which we want conditional probabilities
						List<APopulationValue> aa =  new ArrayList<>(a);
						// att2value.values()
						
						//this.segmentedMatrix.getMatricesInvolving(val).stream().filter(matrix -> att2value.containsKey(key));
						// ... and for this specific val
						aa.add(val);
						// TODO sometimes I've here a NUllpointerexception when one of the values if empty (typically Age3)
						try {
							logger.debug("\t\tfor aspects: {}, getVal returns {}", aa, this.segmentedMatrix.getVal(aa));
							Double v = this.segmentedMatrix.getVal(aa).getValue();
							total += v;
							distribution.add(v);
						} catch (NullPointerException e) {
							logger.warn("\t\tpotential value {} will be excluded from the distribution as it has no probability", val);
						}
					}
					APopulationValue theOne = null;
					if (distribution.isEmpty() || total==0.) {
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
	
	/**
	 * Based on a list of generated individuals, computes the delta between the available data 
	 * and the generated population. 
	 * @param generated
	 */
	@Override
	public ISamplingEvaluation evaluateQuality(IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> population) {

		BasicSamplingEvaluation evaluation = new BasicSamplingEvaluation(population.size());
		
		double totalMSE = .0;
		
		// for each matrix, recompute the probabilities
		for (AFullNDimensionalMatrix<Double> mParam: segmentedMatrix.getMatrices()) {
			
			AFullNDimensionalMatrix<Integer> mMeasuredContigency = new GosplDistributionFactory().createContigencyFromPopulation(mParam.getDimensions(), population);
			
			AFullNDimensionalMatrix<Double> mMeasuredFrequency = new GosplDistributionFactory().createGlobalFrequencyTableFromContingency(mMeasuredContigency);
			
			System.out.println("reference: ");
			System.out.println(mParam);
			
			System.out.println("generated: ");
			System.out.println(mMeasuredFrequency);
			
			double msq = SamplingEvaluationUtils.computeMeanSquarredError(mParam, mMeasuredFrequency);
			totalMSE += msq;
			System.out.println("error: "+msq);
			// compute difference between the matrices
			
		}
		
		evaluation.setOverallBias(totalMSE/segmentedMatrix.getMatrices().size());
		return evaluation;
		
	}

	// -------------------- utility -------------------- //

	@Override
	public String toCsv(String csvSeparator) {
		// TODO Auto-generated method stub
		return null;
	}


}
