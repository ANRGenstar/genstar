package gospl.algo.ipf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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
 * Two concerns must be cleared up for {@link GosplIPF} to be fully and explicitly setup: 
 * <p>
 * <ul>
 * <li> Convergence criteria: could be a number of maximum iteration {@link GosplIPF#MAX_STEP} or
 * a maximal error for any objectif {@link GosplIPF#MAX_DELTA}
 * <li> zero-cell problem: to provide a fit IPF procedure must not encounter any zero-cell or zero-control.
 * there are replaced by small values, calculated as a ratio - {@link GosplIPF#ZERO_CELL_RATIO} - of the smallest 
 * value in matrix or control
 * </ul>
 * <p>
 * Usefull information could be found at {@link http://u.demog.berkeley.edu/~eddieh/datafitting.html}
 * 
 * @author kevinchapuis
 *
 */
public abstract class GosplIPF<T extends Number> {
	
	public static int MAX_STEP = 1000;
	public static double MAX_DELTA = Math.pow(10, -2);
	
	public static double ZERO_CELL_RATIO = Math.pow(10, -3);
	
	protected IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> seed;
	protected INDimensionalMatrix<APopulationAttribute, APopulationValue, T> matrix;

	/**
	 * TODO: javadoc
	 * 
	 * @param seed
	 * @param matrix
	 */
	protected GosplIPF(IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> seed) {
		this.seed = seed;
	}
	
	protected void setMarginalMatrix(INDimensionalMatrix<APopulationAttribute, APopulationValue, T> matrix){
		this.matrix = matrix;
	}
	
	//////////////////////////////////////////////////////////////
	// ------------------------- ALGO ------------------------- //
	//////////////////////////////////////////////////////////////
	
	
	public AFullNDimensionalMatrix<T> process() {
		return process(MAX_DELTA, MAX_STEP);
	}
	
	public AFullNDimensionalMatrix<T> process(int step) {
		return process(MAX_DELTA, step);
	}
	
	public AFullNDimensionalMatrix<T> process(double delta) {
		return process(delta, MAX_STEP);
	}
	
	public abstract AFullNDimensionalMatrix<T> process(double delta, int step);
	
	// ------------------------- UTILITIES ------------------------- //
	
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
