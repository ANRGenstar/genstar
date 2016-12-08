package gospl.algo.ipf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
 * TODO: yet to implement
 * <p>
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
	
	protected IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> seed;
	protected INDimensionalMatrix<APopulationAttribute, APopulationValue, T> matrix;

	/**
	 * TODO: javadoc
	 * 
	 * @param seed
	 * @param matrix
	 */
	protected AGosplIPF(IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> seed) {
		this.seed = seed;
	}
	
	/**
	 * Setup the matrix that define marginal control. May be a full or segmented matrix: the first one
	 * will give actual marginal, while the second one will give estimate marginal
	 * 
	 * @see INDimensionalMatrix#getVal(Collection)
	 * @param matrix
	 */
	protected void setMarginalMatrix(INDimensionalMatrix<APopulationAttribute, APopulationValue, T> matrix){
		this.matrix = matrix;
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
	
	
	public AFullNDimensionalMatrix<T> process() {
		return process(delta, step);
	}
	
	public abstract AFullNDimensionalMatrix<T> process(double delta, int step);
	
	// ------------------------- GENERIC IPF ------------------------- //
	
	/**
	 * Describe the <i>estimation factor</i> IPF algorithm:
	 * <p>
	 * <ul>
	 * <li> Setup/update convergence criteria and iterate while criteria is not fulfill
	 * <li> Compute the factor to fit control
	 * <li> Adjust dimensional values to fit control
	 * </ul>
	 * <p>
	 * There is other algorithm for IPF. This one is the most simple one and also the more
	 * adaptable to a n-dimensional matrix, because it does not include any matrix calculation
	 * 
	 * @param distribution
	 * @return
	 */
	protected AFullNDimensionalMatrix<T> process(AFullNDimensionalMatrix<T> distribution) {
		if(!matrix.getDimensions().equals(distribution.getDimensions()) ||
				!matrix.getDimensions().equals(seed.getPopulationAttributes())) 
			throw new IllegalArgumentException("Output ditribution and sample seed cannot have divergent dimensions\n"
					+ "Distribution: "+Arrays.toString(matrix.getDimensions().toArray()) +"\n"
					+ "Sample seed: :"+Arrays.toString(seed.getPopulationAttributes().toArray()));
		
		// Setup IPF main argument
		Map<APopulationAttribute, Map<Set<APopulationValue>, AControl<T>>> marginals = this.getMarginalValues(true);
		List<APopulationAttribute> attributesList = new ArrayList<>(this.matrix.getDimensions());
		
		// First: establish convergence criteria
		List<Double> errors = IntStream.range(0, this.matrix.getDimensions().size())
				.mapToObj(i -> Double.POSITIVE_INFINITY).collect(Collectors.toList());
		List<Double> deltas = attributesList.stream().map(att -> marginals.get(att).values()
				.stream().mapToDouble(control -> control.getValue().doubleValue()).sum() * delta)
				.collect(Collectors.toList());
		
		// Iterate while one of the criteria is not reach
		while(step-- > 0 || IntStream.range(0, attributesList.size())
				.allMatch(i -> errors.get(i) <= deltas.get(i))){
			// For each dimension
			for(APopulationAttribute attribute : attributesList){
				// For each marginal
				for(Entry<Set<APopulationValue>, AControl<T>> entry : 
					marginals.get(attribute).entrySet()){
					// Compute correction factor
					AControl<Double> factor = new ControlFrequency(entry.getValue().getValue().doubleValue() / 
							distribution.getVal(entry.getKey()).getValue().doubleValue());
					// For each value
					for(APopulationValue value : attribute.getValues()){
						distribution.getVal(new GosplCoordinate(Stream.concat(entry.getKey().stream(), 
								Stream.of(value)).collect(Collectors.toSet()))).multiply(factor);
					}
				}
			}
		}
		return distribution;
	}
	
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
	protected Collection<Set<APopulationValue>> getMarginalDescriptors(APopulationAttribute referent){
		Collection<Set<APopulationValue>> marginalDescriptors = new ArrayList<>();
		for(APopulationAttribute att : matrix.getDimensions()){
			if(att.equals(referent))
				continue;
			if(marginalDescriptors.isEmpty()){
				att.getValues().stream().forEach(value -> 
					marginalDescriptors.add(Stream.of(value).collect(Collectors.toSet())));
			} else {
				Collection<Set<APopulationValue>> tmpDescriptors = new ArrayList<>();
				for(Set<APopulationValue> descriptors : marginalDescriptors){
					tmpDescriptors.addAll(att.getValues().stream()
							.map(val -> Stream.concat(descriptors.stream(), Stream.of(val)).collect(Collectors.toSet()))
									.collect(Collectors.toList())); 
				}
			}
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
	 * WARNING: let the user define whatever this method uses {@link Stream#parallel()} capabilities 
	 * 
	 * @param parallel
	 * @return
	 */
	protected Map<APopulationAttribute, Map<Set<APopulationValue>, AControl<T>>> getMarginalValues(boolean parallel){
		if(parallel)
			return matrix.getDimensions().parallelStream().collect(Collectors.toMap(att -> att, att -> getMarginalDescriptors(att)
					.stream().collect(Collectors.toMap(valSet -> valSet, valSet -> matrix.getVal(valSet)))));
		return matrix.getDimensions().stream().collect(Collectors.toMap(att -> att, att -> getMarginalDescriptors(att)
					.stream().collect(Collectors.toMap(valSet -> valSet, valSet -> matrix.getVal(valSet)))));
	}
	
}
