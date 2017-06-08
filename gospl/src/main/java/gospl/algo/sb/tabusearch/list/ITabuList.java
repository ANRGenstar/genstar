package gospl.algo.sb.tabusearch.list;

import gospl.algo.sb.metamodel.IGSSampleBasedCOSolution;

/**
 * Tabu list interface
 * @author Alex Ferreira
 *
 */
public interface ITabuList extends Iterable<IGSSampleBasedCOSolution> {
	
	/**
	 * Add some solution to the tabu
	 * @param solution the solution to be added
	 */
	void add(IGSSampleBasedCOSolution solution);
	
	/**
	 * Check if a given solution is inside of this tabu list
	 * @param solution the solution to check
	 * @return true if the given solution is contained by this tabu, false otherwise
	 */
	Boolean contains(IGSSampleBasedCOSolution solution);
	
	/**
	 * Update the size of the tabu list dinamically<br>
	 * This method should be implemented only by dynamic sized tabu lists, and may be called after each iteration of the algorithm
	 * @param currentIteration the current iteration of the algorithm
	 * @param bestSolutionFound the best solution found so far
	 */
	void updateSize(Integer currentIteration, IGSSampleBasedCOSolution bestSolutionFound);

}