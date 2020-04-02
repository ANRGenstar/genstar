package gospl.algo.co.hillclimbing;

import java.util.Map;

import org.apache.logging.log4j.Level;

import core.util.GSPerformanceUtil;
import gospl.algo.co.metamodel.AMultiLayerOptimizationAlgorithm;
import gospl.algo.co.metamodel.neighbor.IPopulationNeighborSearch;
import gospl.algo.co.metamodel.neighbor.PopulationEntityNeighborSearch;
import gospl.algo.co.metamodel.solution.MultiLayerSPSolution;

public class MultiHillClimbing extends AMultiLayerOptimizationAlgorithm {

	int nbIteration;
	
	public MultiHillClimbing(int nbIteration, double fitnessThreshold) {
		this(new PopulationEntityNeighborSearch(), nbIteration, fitnessThreshold);
	}
	
	public MultiHillClimbing(IPopulationNeighborSearch<?> neighborSearch, int nbIteration, double fitnessThreshold) {
		this(neighborSearch, nbIteration, 50/100, fitnessThreshold);
	}
	
	public MultiHillClimbing(IPopulationNeighborSearch<?> neighborSearch, int nbIteration, double k_neighborRatio, double fitnessThreshold) {
		super(neighborSearch, fitnessThreshold);
		this.nbIteration = nbIteration;
		this.setK_neighborRatio(k_neighborRatio);
	}

	@Override
	public MultiLayerSPSolution run(MultiLayerSPSolution initialSolution) {
		GSPerformanceUtil gspu = new GSPerformanceUtil("Start HIll Climbing Algorithm\n"
				+ "Population size = "+initialSolution.getSolution().size()
				+ "\nSample size = "+super.getSample().size()
				+ "\nMax iteration = "+nbIteration
				+ "\nNeighbor search = "+super.getNeighborSearchAlgorithm().getClass().getSimpleName()
				+ "\nSolution = "+initialSolution.getClass().getSimpleName(), 
				Level.DEBUG);
		gspu.setObjectif(nbIteration);
		
		MultiLayerSPSolution bestSolution = initialSolution;
		
		// WARNING : strong hypothesis in fitness aggregation, better use pareto frontier
		Double bestFitness = this.getFitness(bestSolution.getFitness(this.getLayeredObjectives()));
		
		super.getNeighborSearchAlgorithm().updatePredicates(initialSolution.getSolution());
		
		int iter = 0;
		int buffer = this.computeBuffer(bestFitness, initialSolution);
		
		gspu.sysoStempMessage("Initial fitness: "+bestFitness);
		
		while(iter++ < nbIteration && bestFitness > this.getFitnessThreshold()) {
			MultiLayerSPSolution candidateState = bestSolution.getRandomNeighbor(
					super.getNeighborSearchAlgorithm(), buffer);
			double currentFitness = this.getFitness(candidateState.getFitness(this.getLayeredObjectives()));
			gspu.sysoStempMessage("Found a new solution (buffer = "+buffer+") with fitness: "+bestFitness);
			if(currentFitness < bestFitness) {
				bestSolution = candidateState;
				bestFitness = currentFitness;
				super.getNeighborSearchAlgorithm().updatePredicates(bestSolution.getSolution());
				buffer = super.computeBuffer(bestFitness, bestSolution);
			}
			if(iter % (nbIteration / 10) == 0) {
				gspu.sysoStempPerformance(iter/gspu.getObjectif(), this);
				gspu.sysoStempMessage("Best fitness = "+bestFitness +" (buffer = "+buffer+") | Pop size = "
						+bestSolution.getSolution().size());
				gspu.sysoStempMessage("BF = "+bestFitness+" | CF = "+currentFitness);
			}
		}
		
		return bestSolution;
	}
	
	/*
	 * Aggregate layer based fitness, must satisfy identity (e.i. if only one fitness return fitness)
	 */
	private double getFitness(Map<Integer,Double> layeredFitness) {
		return layeredFitness.values().stream().mapToDouble(f -> f).average().getAsDouble();
	}

}
