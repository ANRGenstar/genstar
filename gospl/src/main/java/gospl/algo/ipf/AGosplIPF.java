package gospl.algo.ipf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.metamodel.IPopulation;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;
import gospl.algo.sampler.IDistributionSampler;
import gospl.algo.sampler.IEntitySampler;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.ASegmentedNDimensionalMatrix;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.distribution.matrix.control.AControl;
import gospl.distribution.matrix.control.ControlFrequency;
import gospl.distribution.matrix.coordinate.GosplCoordinate;

/**
 * 
 * Higher order abstraction that contains basics principle calculation for IPF. <br>
 * Concrete classes should provide two type of outcomes:
 * <p>
 * <ul>
 * <li> one to draw from a distribution using a {@link IDistributionSampler}
 * <li> ont to draw from a samble using a {@link IEntitySampler}
 * </ul>
 * <p>
 * Two concerns must be cleared up for {@link AGosplIPF} to be fully and explicitly setup: 
 * <p>
 * <ul>
 * <li> Convergence criteria: could be a number of maximum iteration {@link AGosplIPF#step} or
 * a maximal error for any objectif {@link AGosplIPF#delta}
 * <li> zero-cell problem: to provide a fit IPF procedure must not encounter any zero-cell or zero-control.
 * there are replaced by small values, calculated as a ratio - {@link AGosplIPF#ZERO_CELL_RATIO} - of the smallest 
 * value in matrix or control
 * </ul>
 * <p>
 * Usefull information could be found at {@link http://u.demog.berkeley.edu/~eddieh/datafitting.html}
 * 
 * @author kevinchapuis
 *
 */
public abstract class AGosplIPF<T extends Number> {

	private int step = 1000;
	private double delta = Math.pow(10, -2);

	public static double ZERO_CELL_RATIO = Math.pow(10, -3);

	private Logger logger = LogManager.getLogger();

	protected IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> sampleSeed;
	protected INDimensionalMatrix<APopulationAttribute, APopulationValue, T> marginals;

	protected AGosplIPF(IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> sampleSeed){
		this.sampleSeed = sampleSeed;
	}

	protected AGosplIPF(IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> sampleSeed,
			int step, double delta){
		this.sampleSeed = sampleSeed;
		this.step = step;
		this.delta = delta;
	}

	/**
	 * Setup the matrix that define marginal control. May be a full or segmented matrix: the first one
	 * will give actual marginal, while the second one will give estimate marginal
	 * 
	 * @see INDimensionalMatrix#getVal(Collection)
	 * @param marginals
	 */
	protected void setMarginalMatrix(INDimensionalMatrix<APopulationAttribute, APopulationValue, T> marginals){
		this.marginals = marginals;
	}

	/**
	 * Setup iteration number stop criteria
	 * 
	 * @param maxStep
	 */
	protected void setMaxStep(int maxStep){
		this.step = maxStep;
	}

	/**
	 * Setup maximum delta (i.e. the relative absolute difference between actual and expected marginals)
	 * stop criteria 
	 * 
	 * @param delta
	 */
	protected void setMaxDelta(double delta) {
		this.delta = delta;
	}

	//////////////////////////////////////////////////////////////
	// ------------------------- ALGO ------------------------- //
	//////////////////////////////////////////////////////////////


	public abstract AFullNDimensionalMatrix<T> process();

	public AFullNDimensionalMatrix<T> process(double delta, int step){
		this.delta = delta;
		this.step = step;
		return process();
	}

	// ------------------------- GENERIC IPF ------------------------- //

	/**
	 * Describe the <i>estimation factor</i> IPF algorithm:
	 * <p>
	 * <ul>
	 * <li> Setup convergence criteria and iterate while criteria is not fulfill
	 * <li> Compute the factor to fit control
	 * <li> Adjust dimensional values to fit control
	 * <li> Update convergence criteria
	 * </ul>
	 * <p>
	 * There is other algorithm for IPF. This one is the most simple one and also the more
	 * adaptable to a n-dimensional matrix, because it does not include any matrix calculation
	 * 
	 * @param seed
	 * @return
	 */
	protected AFullNDimensionalMatrix<T> process(AFullNDimensionalMatrix<T> seed) {
		if(!marginals.getDimensions().equals(seed.getDimensions())) 
			throw new IllegalArgumentException("Output ditribution and sample seed cannot have divergent dimensions\n"
					+ "Distribution: "+Arrays.toString(marginals.getDimensions().toArray()) +"\n"
					+ "Sample seed: :"+Arrays.toString(seed.getDimensions().toArray()));

		// Some debug purpose log
		logger.trace("Sample seed controls' dimension: \n{}", seed.getDimensions().stream().map(d -> d.getAttributeName()+" = "+seed.getVal(d.getValues()))
				.reduce((s1, s2) -> s1.concat("\n"+s2)));

		// Setup IPF main argument
		Map<APopulationAttribute, Map<Set<APopulationValue>, AControl<T>>> marginals = this.getMarginalCellsPerAttribute(this.marginals, true);
		List<APopulationAttribute> attributesList = new ArrayList<>(this.marginals.getDimensions());

		// First: establish convergence criteria
		int stepIter = step;
		boolean convergentDelta = false;

		logger.trace("Convergence criteria are: step = {} | delta = {}", step, delta);
		logger.trace("Start fitting iterations");

		// Iterate while one of the criteria is not reach
		while(stepIter-- > 0 ? !convergentDelta : false){
			if(stepIter % (int) (step * 0.1) == 0d)
				logger.trace("Step = {} | convergence {}", step - stepIter, convergentDelta);
			// For each dimension
			for(APopulationAttribute attribute : attributesList){
				// For each marginal
				for(Entry<Set<APopulationValue>, AControl<T>> entry : 
					marginals.get(attribute).entrySet()){
					// Compute correction factor
					AControl<Double> factor = new ControlFrequency(entry.getValue().getValue().doubleValue() / 
							seed.getVal(entry.getKey()).getValue().doubleValue());
					// For each value
					for(APopulationValue value : attribute.getValues()){
						try {
							seed.getVal(new GosplCoordinate(Stream.concat(entry.getKey().stream(), 
									Stream.of(value)).collect(Collectors.toSet()))).multiply(factor);
						} catch (NullPointerException e) {
							// ZERO CELL PROBLEM
						}
					}
				}
			}
			Map<APopulationAttribute, Map<Set<APopulationValue>, AControl<T>>> actualMarginals = getMarginalCellsPerAttribute(seed, true);
			if(actualMarginals.entrySet().stream().allMatch(entry -> 
				marginals.get(entry.getKey()).entrySet().stream().allMatch(marginal -> 
					entry.getValue().get(marginal.getKey()).equalsVal(marginal.getValue(), delta))))
				convergentDelta = true;
			logger.trace("There is some delta exceeding convergence criteria\n{}", actualMarginals.entrySet().stream()
					.map(eActual -> eActual.getKey().getAttributeName()+" IPF computed values:\n"+
							eActual.getValue().entrySet().stream().map(vActual -> Arrays.toString(vActual.getKey().toArray())
									+" => "+vActual.getValue()+" | "+marginals.get(eActual.getKey()).get(vActual.getKey()))
							.reduce("", (s1, s2) -> s1.concat(s2+"\n")))
					.reduce("", (s1, s2) -> s1.concat(s2)));
		}
		return seed;
	}

	// ---------------------- IPF utilities ---------------------- //

	/**
	 * Return the marginal descriptors coordinate for a referent dimension:
	 * a collection of all attribute's value combination (a set of value) that 
	 * exclude referent attribute; those set describe the marginal for this
	 * referent attribute conditional to all other possible combination of 
	 * attribute's values
	 * 
	 * @param referent
	 * @return
	 */
	private Collection<Set<APopulationValue>> getMarginalDescriptors(APopulationAttribute referent){
		// Setup output
		Collection<Set<APopulationValue>> marginalDescriptors = new ArrayList<>();
		// Define the set of tageted attributes
		Set<APopulationAttribute> targetedAttributes = new HashSet<>(marginals.getDimensions());
		targetedAttributes.remove(referent);
		// Init. the output collection with any attribute
		APopulationAttribute firstAtt = targetedAttributes.iterator().next();
		for(APopulationValue value : firstAtt.getValues())
			marginalDescriptors.add(Stream.of(value).collect(Collectors.toSet()));
		targetedAttributes.remove(firstAtt);
		// Then iterate over all other attributes
		for(APopulationAttribute att : targetedAttributes){
			List<Set<APopulationValue>> tmpDescriptors = new ArrayList<>();
			for(Set<APopulationValue> descriptors : marginalDescriptors){
				tmpDescriptors.addAll(att.getValues().stream()
						.map(val -> Stream.concat(descriptors.stream(), Stream.of(val)).collect(Collectors.toSet()))
						.collect(Collectors.toList())); 
			}
			marginalDescriptors = tmpDescriptors;
		}
		return marginalDescriptors;
	}

	/**
	 * One of the crucial method although very light. All marginals are calculated using 
	 * {@link INDimensionalMatrix#getVal(Collection values)}, hence comprising different
	 * knowledge depending on matrix concrete type: 
	 * <p>
	 * <ul>
	 * <li> {@link AFullNDimensionalMatrix}: gives the real margin
	 * <li> {@link ASegmentedNDimensionalMatrix}: gives accurate margin based on potentially
	 * limited information
	 * </ul>
	 * <p>
	 * WARNING: let the user define if this method should use {@link Stream#parallel()} capabilities or not
	 * 
	 * @param parallel
	 * @return
	 */
	private Map<APopulationAttribute, Map<Set<APopulationValue>, AControl<T>>> getMarginalCellsPerAttribute(
			INDimensionalMatrix<APopulationAttribute, APopulationValue, T> anyDistribution,
			boolean parallel){
		if(parallel)
			return anyDistribution.getDimensions().parallelStream().collect(Collectors.toMap(att -> att, att -> getMarginalDescriptors(att)
					.stream().collect(Collectors.toMap(valSet -> valSet, valSet -> anyDistribution.getVal(valSet)))));
		return anyDistribution.getDimensions().stream().collect(Collectors.toMap(att -> att, att -> getMarginalDescriptors(att)
				.stream().collect(Collectors.toMap(valSet -> valSet, valSet -> anyDistribution.getVal(valSet)))));
	}

}
