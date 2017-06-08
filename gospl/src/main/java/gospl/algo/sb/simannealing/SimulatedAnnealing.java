package gospl.algo.sb.simannealing;

import gospl.algo.sb.metamodel.AGSOptimizationAlgorithm;
import gospl.algo.sb.metamodel.IGSSampleBasedCOSolution;
import gospl.algo.sb.simannealing.transition.GSSADefautTransFunction;
import gospl.algo.sb.simannealing.transition.IGSSimAnnealingTransFonction;

public class SimulatedAnnealing extends AGSOptimizationAlgorithm {

	private int bottomTemp = 1;
	private double minStateEnergy = 0;
	
	private int temperature = 100000;
	private double coolingRate = Math.pow(10, -3);
	
	private IGSSimAnnealingTransFonction transFunction;

	public SimulatedAnnealing(double minStateEnergy,
			int initTemp, double coolingRate, 
			IGSSimAnnealingTransFonction transFonction) {
		this.minStateEnergy = minStateEnergy;
		this.temperature = initTemp;
		this.coolingRate = coolingRate;
	}

	public SimulatedAnnealing(double minStateEnergy,
			IGSSimAnnealingTransFonction transFonction){
		this.minStateEnergy = minStateEnergy;
		this.transFunction = transFonction;
	}
	
	public SimulatedAnnealing(IGSSimAnnealingTransFonction transFunction){
		this.transFunction = transFunction;
	}
	
	public SimulatedAnnealing(){
		this.transFunction = new GSSADefautTransFunction();
	}
	
	@Override
	public IGSSampleBasedCOSolution run(IGSSampleBasedCOSolution initialSolution){
		
		IGSSampleBasedCOSolution currentState = initialSolution;
		IGSSampleBasedCOSolution bestState = initialSolution;
		
		double currentEnergy = currentState.getFitness(this.getObjectives());
		double bestEnergy = currentEnergy;
		
		// Iterate while system temperature is above cool threshold 
		// OR while system energy is above minimum state energy
		while(temperature < bottomTemp ||
				currentEnergy > minStateEnergy){
			// Elicit a random new candidate for a transition state
			IGSSampleBasedCOSolution systemStateCandidate = currentState.getRandomNeighbor();
			double candidateEnergy = systemStateCandidate.getFitness(this.getObjectives());
			
			// IF probability function elicit transition state
			// THEN change current state to be currentCandidate 
			if(transFunction.getTransitionProbability(currentEnergy, candidateEnergy, temperature)){
				currentState = systemStateCandidate;
				currentEnergy = currentState.getFitness(this.getObjectives());
			}
			
			// Keep track of best state visited
			if(bestEnergy > currentEnergy){
				bestState = currentState;
				bestEnergy = currentEnergy;
			}
			
			// Cool down system temperature
			temperature *= 1 - coolingRate;
		}
		
		return bestState;
	}
	
}
