package gospl.algo.ipf;

import java.util.Collection;

import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;
import gospl.algo.sampler.IDistributionSampler;
import gospl.algo.sampler.IEntitySampler;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.INDimensionalMatrix;

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
 * a maximal error for any objectif {@link GosplIPF#CONVERGENCE_DELTA}
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
	public static double CONVERGENCE_DELTA = Math.pow(10, -2);
	
	public static double ZERO_CELL_RATIO = Math.pow(10, -3);
	
	protected Collection<APopulationEntity> seed;
	protected INDimensionalMatrix<APopulationAttribute, APopulationValue, T> matrix;

	/**
	 * TODO: javadoc
	 * 
	 * @param seed
	 * @param matrix
	 */
	protected GosplIPF(Collection<APopulationEntity> seed) {
		this.seed = seed;
	}
	
	protected void setMarginalMatrix(INDimensionalMatrix<APopulationAttribute, APopulationValue, T> matrix){
		this.matrix = matrix;
	}
	
	//////////////////////////////////////////////////////////////
	// ------------------------- ALGO ------------------------- //
	//////////////////////////////////////////////////////////////
	
	
	public AFullNDimensionalMatrix<T> process() {
		return process(CONVERGENCE_DELTA, MAX_STEP);
	}
	
	public AFullNDimensionalMatrix<T> process(int step) {
		return process(CONVERGENCE_DELTA, step);
	}
	
	public AFullNDimensionalMatrix<T> process(double convergenceDelta) {
		return process(convergenceDelta, MAX_STEP);
	}
	
	public abstract AFullNDimensionalMatrix<T> process(double convergenceDelta, int step);
	
}
