package gospl.algo.sb.metamodel;

import java.util.Set;

import gospl.distribution.matrix.AFullNDimensionalMatrix;

/**
 * Main interfaces for Genstar formated optimization algorithm
 * 
 * @author kevinchapuis
 *
 */
public interface IGSOptimizationAlgorithm {

	/**
	 * Execute the algorithm to perform targeted optimization.
	 * @param {@code initialSolution} the start point of the algorithm
	 * @return the best solution found in the given conditions
	 */
	public IGSSampleBasedCOSolution run(IGSSampleBasedCOSolution initialSolution);
	
	/**
	 * Retrieve the set of objectives this optimization algorithm is
	 * calibrated with
	 * @return
	 */
	public Set<AFullNDimensionalMatrix<Integer>> getObjectives();
	
	/**
	 * Add objectives to assess solution goodness-of-fit
	 * @param objectives
	 */
	public void addObjectives(AFullNDimensionalMatrix<Integer> objectives);
	
}
