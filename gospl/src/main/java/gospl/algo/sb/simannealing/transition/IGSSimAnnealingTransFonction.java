package gospl.algo.sb.simannealing.transition;

public interface IGSSimAnnealingTransFonction {

	public boolean getTransitionProbability(double currentEnergy, double candidateEnergy, int temperature);
	
}
