package gospl.algo.sb.simannealing.transition;

import core.util.random.GenstarRandom;

public class GSSADefautTransFunction implements IGSSimAnnealingTransFonction {

	@Override
	public boolean getTransitionProbability(double currentEnergy, double candidateEnergy, int temperature) {
		if(currentEnergy > candidateEnergy)
			return true;
		return Math.exp((currentEnergy - candidateEnergy) / temperature) > GenstarRandom.getInstance().nextDouble();
	}


}
