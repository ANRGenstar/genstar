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
	public TabuSearch(ITabuList tabulist, int maxIterations) {
		this(new PopulationEntityNeighborSearch(), tabulist, maxIterations);
	}
	
	public TabuSearch(IPopulationNeighborSearch<?> neighborSearch,
			ITabuList tabuList, int maxIterations) {
		super(neighborSearch);
		this.tabuList = tabuList;
		this.maxIterations = maxIterations;
	}
	
	@Override
	public ISyntheticPopulationSolution run(ISyntheticPopulationSolution initialSolution) {
		ISyntheticPopulationSolution bestSolution = initialSolution;
		ISyntheticPopulationSolution currentSolution = initialSolution;
		
		GSPerformanceUtil gspu = new GSPerformanceUtil(
				"Start Tabu Search algorithm in CO synthetic population generation process", 
				Level.DEBUG);
		
		Integer currentIteration = 0;
		while (++currentIteration < this.maxIterations) {
			
			/*
			if(currentIteration % (this.maxIterations / 10d) == 0)
				gspu.sysoStempPerformance(0.1, this);
				*/
			gspu.sysoStempPerformance("iter "+currentIteration, this);
			
			List<ISyntheticPopulationSolution> solutionsInTabu = new ArrayList<>();
			tabuList.iterator().forEachRemaining(solutionsInTabu::add);
			
			gspu.sysoStempPerformance("Retrieve neighbors from current solution", this);
			Collection<ISyntheticPopulationSolution> neighbors = currentSolution.getNeighbors(
					super.getNeighborSearchAlgorithm());
			
			gspu.sysoStempPerformance("Start eliciting best neighbors", this);
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
