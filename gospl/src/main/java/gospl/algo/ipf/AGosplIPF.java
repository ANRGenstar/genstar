package gospl.algo.ipf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.metamodel.IPopulation;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;
import gospl.algo.ipf.util.IMarginalsIPFProcessor;
import gospl.algo.ipf.util.MarginalsIPFProcessor;
import gospl.algo.sampler.IDistributionSampler;
import gospl.algo.sampler.IEntitySampler;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.distribution.matrix.control.AControl;
import gospl.distribution.matrix.control.ControlFrequency;
import gospl.distribution.matrix.coordinate.ACoordinate;

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
	protected IMarginalsIPFProcessor<T> zeroCell;

	protected AGosplIPF(IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> sampleSeed,
			IMarginalsIPFProcessor<T> zeroCell, int step, double delta){
		this.sampleSeed = sampleSeed;
		this.zeroCell = zeroCell;
		this.step = step;
		this.delta = delta;
	}

	protected AGosplIPF(IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> sampleSeed,
			int step, double delta){
		this(sampleSeed, new MarginalsIPFProcessor<T>(), step, delta);
	}
	
	protected AGosplIPF(IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> sampleSeed,
			IMarginalsIPFProcessor<T> zeroCell){
		this.sampleSeed = sampleSeed;
		this.zeroCell = zeroCell;
	}

	protected AGosplIPF(IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> sampleSeed){
		this(sampleSeed, new MarginalsIPFProcessor<T>());
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
		if(seed.getDimensions().stream().noneMatch(dim -> marginals.getDimensions().contains(dim) || 
				marginals.getDimensions().contains(dim.getReferentAttribute())))
			throw new IllegalArgumentException("Output distribution and sample seed does not have any matching dimensions\n"
					+ "Distribution: "+Arrays.toString(marginals.getDimensions().toArray()) +"\n"
					+ "Sample seed: :"+Arrays.toString(seed.getDimensions().toArray()));

		List<APopulationAttribute> unmatchSeedAttribute = seed.getDimensions().stream()
				.filter(dim -> marginals.getDimensions().contains(dim) 
						|| marginals.getDimensions().contains(dim.getReferentAttribute()))
				.collect(Collectors.toList());

		logger.trace("{}% of samples dimensions will be estimate with output controls", 
				unmatchSeedAttribute.size() / (double) seed.getDimensions().size() * 100d);
		logger.trace("Sample seed controls' dimension: \n{}", seed.getDimensions().stream().map(d -> d.getAttributeName()+" = "+seed.getVal(d.getValues()))
				.reduce((s1, s2) -> s1.concat("\n"+s2)));

		Map<APopulationAttribute, Map<Set<APopulationValue>, AControl<T>>> marginals = zeroCell.buildCompliantMarginals(this.marginals, seed, true);

		int stepIter = step;
		boolean convergentDelta = false;
		logger.trace("Convergence criteria are: step = {} | delta = {}", step, delta);
		logger.trace("Start fitting iterations");

		while(stepIter-- > 0 ? !convergentDelta : false){
			if(stepIter % (int) (step * 0.1) == 0d)
				logger.trace("Step = {} | convergence {}", step - stepIter, convergentDelta);
			for(APopulationAttribute attribute : marginals.keySet()){
				for(Entry<Set<APopulationValue>, AControl<T>> entry : 
					marginals.get(attribute).entrySet()){
					AControl<Double> factor = new ControlFrequency(entry.getValue().getValue().doubleValue() / 
							seed.getVal(entry.getKey()).getValue().doubleValue());
					Collection<ACoordinate<APopulationAttribute, APopulationValue>> relatedCoordinates = 
							seed.getCoordinates(entry.getKey()); 
					for(ACoordinate<APopulationAttribute, APopulationValue> coord : relatedCoordinates)
						seed.getVal(coord).multiply(factor);
					logger.trace("Work on value set {} and related {} coordinates; factor = {}",
							Arrays.toString(entry.getKey().toArray()), relatedCoordinates.size(), factor.getValue());
				}
			}
			Map<APopulationAttribute, Map<Set<APopulationValue>, AControl<T>>> actualMarginals = getMarginals(seed, 
					Collections.emptyList(), true);
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
	 * Defines and retrieves marginal from a n-dimensional matrix
	 * 
	 * @param control
	 * @param attributes
	 * @param parallel
	 * @return
	 */
	private Map<APopulationAttribute, Map<Set<APopulationValue>, AControl<T>>> getMarginals(
			AFullNDimensionalMatrix<T> matrix, Collection<APopulationAttribute> attributes, boolean parallel) {
		if(!matrix.getDimensions().containsAll(attributes))
			throw new IllegalArgumentException("All attributes must be part of the control matrix");
		Stream<APopulationAttribute> attStream;
		if(parallel)
			attStream = attributes.parallelStream();
		else
			attStream = attributes.stream();
		return attStream.collect(Collectors.toMap(Function.identity(), att -> 
			getMarginalDescriptors(attributes.stream().filter(a -> !a.equals(att)).collect(Collectors.toSet()))
				.stream().collect(Collectors.toMap(Function.identity(), valset -> matrix.getVal(valset)))));
	}
	
	private Collection<Set<APopulationValue>> getMarginalDescriptors(Collection<APopulationAttribute> targetedAttributes){
		// Setup output
		Collection<Set<APopulationValue>> marginalDescriptors = new ArrayList<>();
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

}