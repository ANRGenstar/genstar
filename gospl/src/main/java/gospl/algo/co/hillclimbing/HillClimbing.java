package gospl.algo.co.hillclimbing;

import org.apache.logging.log4j.Level;

import core.util.GSPerformanceUtil;
import gospl.algo.co.metamodel.AOptimizationAlgorithm;
import gospl.algo.co.metamodel.neighbor.IPopulationNeighborSearch;
import gospl.algo.co.metamodel.neighbor.PopulationEntityNeighborSearch;
import gospl.algo.co.metamodel.solution.ISyntheticPopulationSolution;

/**
 * Implement random search optimization algorithm for CO based synthetic population generation <p/>
 * Algorithm includes buffer sized neighboring to explore 'far solutions' when fitness is poor and
 * narrowing neighborhood process when fitness gets better. This means that the number of predicate
 * to asses population neighbor is a linear function of fitness, see {@link IPopulationNeighborSearch}
 * 
 * @author kevinchapuis
 *
 */
public class HillClimbing extends AOptimizationAlgorithm {

	private int nbIteration;
	private double maxBuffer = 0.01;

	public HillClimbing(double fitnessThreshold, int nbIteration) {
		this(new PopulationEntityNeighborSearch(), fitnessThreshold, nbIteration);
	}
	
	public HillClimbing(IPopulationNeighborSearch<?> neighborSearch, double fitnessThreshold, int nbIteration) {
		super(neighborSearch, fitnessThreshold);
		this.nbIteration = nbIteration;
	}
		
	@Override
	public ISyntheticPopulationSolution run(ISyntheticPopulationSolution initialSolution) {
		
		GSPerformanceUtil gspu = new GSPerformanceUtil("Start Random Walk Algorithm\n"
				+ "Population size = "+initialSolution.getSolution().size()
				+ "\nSample size = "+super.getSample().size()
				+ "\nMax iteration = "+nbIteration, Level.TRACE);
		gspu.setObjectif(nbIteration);
		
		ISyntheticPopulationSolution bestSolution = initialSolution;
		double bestFitness = bestSolution.getFitness(this.getObjectives());
		super.getNeighborSearchAlgorithm().updatePredicates(initialSolution.getSolution());
		
		int iter = 0;
		int buffer = this.computeBuffer(bestFitness, initialSolution);
		
		while(iter++ < nbIteration && bestFitness > this.getFitnessThreshold()) {
			ISyntheticPopulationSolution candidateState = bestSolution.getRandomNeighbor(
					super.getNeighborSearchAlgorithm(), buffer);
			double currentFitness = candidateState.getFitness(this.getObjectives()); 
			if(currentFitness < bestFitness) {
				bestSolution = candidateState;
				bestFitness = currentFitness;
				super.getNeighborSearchAlgorithm().updatePredicates(bestSolution.getSolution());
				buffer = this.computeBuffer(bestFitness, bestSolution);
			}
			if(iter % (nbIteration / 100) == 0)
				gspu.sysoStempPerformance(iter/gspu.getObjectif(), "\n"
						+ "Best fitness = "+bestFitness +"(buffer = "+buffer+") | Pop size = "
						+bestSolution.getSolution().size(), this);
		}
		
		return bestSolution;
	}
	
	private int computeBuffer(double fitness, ISyntheticPopulationSolution solution) {
		return Math.round(Math.round(solution.getSolution().size() * maxBuffer
				* (fitness / (solution.getSolution().size() * 
						solution.getSolution().getPopulationAttributes().stream()
						.mapToInt(a -> a.getValueSpace().getValues().size()).reduce(1, 
								(a1,a2) -> a1 != 0 ? a1 : 1 * a2 != 0 ? a2 : 1)))));
	}

}
