package gospl.algo.co.randomwalk;

import gospl.algo.co.metamodel.AOptimizationAlgorithm;
import gospl.algo.co.metamodel.neighbor.IPopulationNeighborSearch;
import gospl.algo.co.metamodel.neighbor.PopulationEntityNeighborSearch;
import gospl.algo.co.metamodel.solution.ISyntheticPopulationSolution;

/**
 * Implement random search optimization algorithm for CO based synthetic population generation
 * 
 * @author kevinchapuis
 *
 */
public class RandomWalk extends AOptimizationAlgorithm {

	private int nbIteration;

	public RandomWalk(int nbIteration) {
		this(new PopulationEntityNeighborSearch(), nbIteration);
	}
	
	public RandomWalk(IPopulationNeighborSearch<?> neighborSearch, int nbIteration) {
		super(neighborSearch);
		this.nbIteration = nbIteration;
	}
		
	@Override
	public ISyntheticPopulationSolution run(ISyntheticPopulationSolution initialSolution) {
		ISyntheticPopulationSolution currentState = initialSolution;
		double currentFitness = currentState.getFitness(this.getObjectives());
		
		int iter = nbIteration;
		int buffer = 1;
		while(iter-- > 0) {
			ISyntheticPopulationSolution candidateState = currentState.getRandomNeighbor(
					super.getNeighborSearchAlgorithm(), buffer++);
			if(candidateState.getFitness(this.getObjectives()) < currentFitness) {
				currentState = candidateState;
				buffer = 1;
			}
		}
		
		return currentState;
	}

}
