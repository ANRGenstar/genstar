package gospl.algo.co.tabusearch;

import java.util.Collection;
import java.util.Optional;

import org.apache.logging.log4j.Level;

import core.util.GSPerformanceUtil;
import core.util.random.GenstarRandomUtils;
import gospl.algo.co.metamodel.AOptimizationAlgorithm;
import gospl.algo.co.metamodel.neighbor.IPopulationNeighborSearch;
import gospl.algo.co.metamodel.neighbor.PopulationAttributeNeighborSearch;
import gospl.algo.co.metamodel.solution.ISyntheticPopulationSolution;

/**
 * Default implementation of the Tabu Search algorithm
 * <p>
 * 1) added k neighbor exploration algorithm @see {@link IPopulationNeighborSearch} <br/>
 * 2) also added mid-term memory process with a random jump when no improvement have been made for 
 * a number of iteration equal to 10% of tabulist size
 * 
 * @author Alex Ferreira
 * @author modified by kevinchapuis
 *
 */
public class TabuSearch extends AOptimizationAlgorithm {

	private ITabuList tabuList;
	private int maxIterations;

	/**
	 * Construct a {@link TabuSearch} object
	 * @param tabuList the tabu list used in the algorithm to handle tabus
	 * @param stopCondition the algorithm stop condition
	 * @param solutionLocator the best neightbor solution locator to be used in each algortithm iteration
	 */
	public TabuSearch(ITabuList tabulist, double fitnessThreshold, int maxIterations) {
		this(new PopulationAttributeNeighborSearch(), tabulist, fitnessThreshold, maxIterations);
	}

	public TabuSearch(IPopulationNeighborSearch<?> neighborSearch,
			ITabuList tabuList, double fitnessThreshold, int maxIterations) {
		super(neighborSearch, fitnessThreshold);
		this.tabuList = tabuList;
		this.maxIterations = maxIterations;
	}

	@Override
	public ISyntheticPopulationSolution run(ISyntheticPopulationSolution initialSolution) {
		ISyntheticPopulationSolution bestSolution = initialSolution;
		ISyntheticPopulationSolution currentSolution = initialSolution;
		this.getNeighborSearchAlgorithm().updatePredicates(initialSolution.getSolution());

		double bestFitness = initialSolution.getFitness(this.getObjectives());

		GSPerformanceUtil gspu = new GSPerformanceUtil(
				"Start Tabu Search algorithm"
						+ "\nPopulation size = "+initialSolution.getSolution().size()
						+ "\nSample size = "+super.getSample().size()
						+ "\nMax iteration = "+this.maxIterations
						+ "\nNeighbor search = "+super.getNeighborSearchAlgorithm().getClass().getSimpleName()
						+ "\nSolution = "+initialSolution.getClass().getSimpleName(), 
						Level.DEBUG);
		gspu.setObjectif(this.maxIterations);

		gspu.sysoStempPerformance(0d, this);
		gspu.sysoStempMessage("Random start solution fitness is "+bestFitness);

		int currentIteration = 0;
		int stuckIdx = 0;
		while (currentIteration++ < this.maxIterations &&
				bestFitness > this.getFitnessThreshold()) {

			if(currentIteration % (this.maxIterations / 10d) == 0) {
				gspu.sysoStempPerformance(currentIteration / gspu.getObjectif(), this);
				gspu.sysoStempMessage("Current fitness is "+bestFitness);
				gspu.sysoStempMessage("Tabu size "+tabuList.getSize()+" "
						+ "| number of stucked iteration "+stuckIdx
						+ " | Current buffer "+super.computeBuffer(bestFitness * stuckIdx, currentSolution));
			}

			// gspu.sysoStempPerformance("Retrieve neighbors from current solution", this);
			Collection<ISyntheticPopulationSolution> neighbors = currentSolution.getNeighbors(
					super.getNeighborSearchAlgorithm());

			// gspu.sysoStempPerformance("Start eliciting best neighbors", this);
			Optional<ISyntheticPopulationSolution> optionalBestSolution = neighbors.stream()
					.filter(candidate -> !this.tabuList.contains(candidate))
					.sorted((s1, s2) -> s1.getFitness(this.getObjectives()).compareTo(s2.getFitness(this.getObjectives())))
					.findFirst(); 

			if(optionalBestSolution.isPresent()) {
				double candidateFitness = optionalBestSolution.get().getFitness(this.getObjectives());
				if(candidateFitness < bestSolution.getFitness(this.getObjectives())) {
					bestSolution = optionalBestSolution.get();
					bestFitness = candidateFitness;
					stuckIdx = 0;
				}
			}
			
			if(GenstarRandomUtils.flip(Math.log1p(stuckIdx++) / Math.log(tabuList.maxSize()))) {
				
				currentSolution = currentSolution.getRandomNeighbor(super.getNeighborSearchAlgorithm(), 
						super.computeBuffer(bestFitness * stuckIdx, currentSolution));
				stuckIdx = 0;

			}	
			
			tabuList.add(currentSolution);
			 

		}

		return bestSolution;
	}

}
