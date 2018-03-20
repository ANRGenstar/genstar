package gospl.algo.co.tabusearch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.Level;

import core.util.GSPerformanceUtil;
import gospl.algo.co.metamodel.AOptimizationAlgorithm;
import gospl.algo.co.metamodel.neighbor.IPopulationNeighborSearch;
import gospl.algo.co.metamodel.neighbor.PopulationEntityNeighborSearch;
import gospl.algo.co.metamodel.solution.ISyntheticPopulationSolution;

/**
 * Default implementation of the Tabu Search algorithm
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
		this(new PopulationEntityNeighborSearch(), tabulist, fitnessThreshold, maxIterations);
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
		
		double bestFitness = initialSolution.getFitness(this.getObjectives());
		
		GSPerformanceUtil gspu = new GSPerformanceUtil(
				"Start Tabu Search algorithm"
				+ "\nPopulation size = "+initialSolution.getSolution().size()
				+ "\nSample size = "+super.getSample().size()
				+ "\nMax iteration = "+this.maxIterations, 
				Level.TRACE);
		gspu.setObjectif(this.maxIterations);
		
		Integer currentIteration = 0;
		while (currentIteration++ < this.maxIterations &&
				bestFitness > this.getFitnessThreshold()) {
			
			boolean doLog = false;
			if(currentIteration % (this.maxIterations / 10d) == 0)
				doLog = true;
			
			if(doLog) {
				gspu.sysoStempPerformance(gspu.getObjectif() / currentIteration, this);
				gspu.sysoStempMessage("Current fitness is "+bestFitness);
			}
			
			List<ISyntheticPopulationSolution> solutionsInTabu = new ArrayList<>();
			tabuList.iterator().forEachRemaining(solutionsInTabu::add);
			
			// if(doLog) gspu.sysoStempPerformance("Retrieve neighbors from current solution", this);
			Collection<ISyntheticPopulationSolution> neighbors = currentSolution.getNeighbors(
					super.getNeighborSearchAlgorithm());
			
			// if(doLog) gspu.sysoStempPerformance("Start eliciting best neighbors", this);
			double bestTabuValue = bestSolution.getFitness(this.getObjectives());
			Optional<ISyntheticPopulationSolution> optionalBestSolution = neighbors.stream()
					.filter(solution -> !solutionsInTabu.contains(solution) &&
							solution.getFitness(this.getObjectives()) > bestTabuValue)
					.sorted((s1, s2) -> s1.getFitness(this.getObjectives()).compareTo(s2.getFitness(this.getObjectives())))
					.findFirst(); 
			
			if(optionalBestSolution.isPresent())
				bestSolution = optionalBestSolution.get();
			tabuList.add(currentSolution);
			currentSolution = bestSolution;
			
		}
		
		return bestSolution;
	}
	
}
