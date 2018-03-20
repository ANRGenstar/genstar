package gospl.algo.co.simannealing;

import org.apache.logging.log4j.Level;

import core.util.GSPerformanceUtil;
import gospl.algo.co.metamodel.AOptimizationAlgorithm;
import gospl.algo.co.metamodel.neighbor.IPopulationNeighborSearch;
import gospl.algo.co.metamodel.neighbor.PopulationEntityNeighborSearch;
import gospl.algo.co.metamodel.solution.ISyntheticPopulationSolution;
import gospl.sampler.IEntitySampler;

/**
 * The simulated annealing algorithm that fit combinatorial optimization framework from 
 * synthetic population field of research. It is used by {@link IEntitySampler} to generate
 * a synthetic population based on a data sample and data aggregated constraints (also called
 * marginals)
 * <p>
 * Basic principles of the algorithm are: we start with a synthetic population (may be randomly generated 
 * from a sample, the sample itself or any user provided synthetic population) which represents the current state. 
 * Each step of the algorithm provide a new population, slightly different from the current state, 
 * which represent a "neighbor state". This new candidate state as a probability to be accepted that depend
 * on its own energy (or fitness) and the temperature of the global system. As algorithm iterates, it will be less
 * incline to accept candidate state with lower energy (worst fitness) and more and more rely on the best candidate
 * he has visited. 
 * 
 * @author kevinchapuis
 *
 */
public class SimulatedAnnealing extends AOptimizationAlgorithm {

	private int bottomTemp = 1;

	private int initTemp = 100000;
	private double coolingRate = Math.pow(10, -3);
	private int transitionLength = 4; 

	private ISimulatedAnnealingTransitionFunction transFunction;

	public SimulatedAnnealing(IPopulationNeighborSearch<?> neighborSearch,
			double minStateEnergy, int initTemp, double coolingRate, 
			ISimulatedAnnealingTransitionFunction transFonction) {
		super(neighborSearch, minStateEnergy);
		this.initTemp = initTemp;
		this.coolingRate = coolingRate;
	}

	public SimulatedAnnealing(double minStateEnergy, int initTemp, double coolingRate, 
			ISimulatedAnnealingTransitionFunction transFonction) {
		super(new PopulationEntityNeighborSearch(), minStateEnergy);
		this.initTemp = initTemp;
		this.coolingRate = coolingRate;
	}


	public SimulatedAnnealing(){
		super(new PopulationEntityNeighborSearch(), 0d);
		this.transFunction = new SimulatedAnnealingDefaultTransitionFunction();
	}

	@Override
	public ISyntheticPopulationSolution run(ISyntheticPopulationSolution initialSolution){

		ISyntheticPopulationSolution currentState = initialSolution;
		ISyntheticPopulationSolution bestState = initialSolution;
		this.getNeighborSearchAlgorithm().updatePredicates(initialSolution.getSolution());
		int nBuffer = (int)(super.getNeighborSearchAlgorithm().getPredicates().size()*super.getK_neighborRatio());

		GSPerformanceUtil gspu = new GSPerformanceUtil(
				"Start Simulated annealing algorithm"
						+ "\nPopulation size = "+initialSolution.getSolution().size()
						+ "\nSample size = "+super.getSample().size()
						+ "\nMin temperature = "+this.bottomTemp, 
						Level.DEBUG);

		double currentEnergy = currentState.getFitness(this.getObjectives());
		double bestEnergy = currentEnergy;

		// Iterate while system temperature is above cool threshold 
		// OR while system energy is above minimum state energy
		double temperature = initTemp;
		int stateTransition = 0;
		int local_transitionLength = transitionLength;
		while(temperature > bottomTemp &&
				currentEnergy > super.getFitnessThreshold()){

			boolean update = false;

			for(int i = 0; i < local_transitionLength; i++) {
				ISyntheticPopulationSolution systemStateCandidate = currentState.getRandomNeighbor(
						super.getNeighborSearchAlgorithm(), nBuffer);
				double candidateEnergy = systemStateCandidate.getFitness(this.getObjectives());

				// IF probability function elicit transition state
				// THEN change current state to be currentCandidate 
				if(transFunction.getTransitionProbability(currentEnergy, candidateEnergy, temperature)){
					gspu.sysoStempPerformance("Updats energy : "
							+ currentEnergy+" -> "+candidateEnergy+" ("+temperature+"°)", this);
					currentState = systemStateCandidate;
					currentEnergy = candidateEnergy;
				}

				// Keep track of best state visited
				if(bestEnergy > currentEnergy){
					bestState = currentState;
					bestEnergy = currentEnergy;
					update = true;
					this.getNeighborSearchAlgorithm().updatePredicates(currentState.getSolution());
				}
			}
			
			if(update) {
				stateTransition++;
				temperature = this.initTemp - coolingRate * stateTransition;
			} else {
				local_transitionLength *= 2;
			}
		}
		gspu.sysoStempPerformance("End simulated annealing with: "
				+"Temperature = "+temperature+" | Energy = "+bestEnergy, this);

		return bestState;
	}

}
