package gospl.algo.co.tabusearch;

import gospl.algo.co.metamodel.solution.ISyntheticPopulationSolution;

/**
 * Tabu list interface
 * @author Alex Ferreira
 *
 */
public interface ITabuList extends Iterable<ISyntheticPopulationSolution> {
	
	/**
	 * Add some solution to the tabu
	 * @param solution the solution to be added
	 */
	public void add(ISyntheticPopulationSolution solution);
	
	/**
	 * Check if a given solution is inside of this tabu list
	 * @param solution the solution to check
	 * @return true if the given solution is contained by this tabu, false otherwise
	 */
	public Boolean contains(ISyntheticPopulationSolution solution);
	
	/**
	 * Update the size of the tabu list dinamically<br>
	 * This method should be implemented only by dynamic sized tabu lists, and may be called after each iteration of the algorithm
	 * @param currentIteration the current iteration of the algorithm
	 * @param bestSolutionFound the best solution found so far
	 */
	public void updateSize(Integer currentIteration, ISyntheticPopulationSolution bestSolutionFound);
	
	public int maxSize();

}