package gospl.algo.sampler.co;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import core.metamodel.pop.APopulationEntity;
import gospl.algo.sb.simannealing.SimulatedAnnealing;
import gospl.algo.sb.simannealing.state.GSSAState;

public class SimAnnealingSampler extends AOptiAlgoSampler<SimulatedAnnealing> {

	public SimAnnealingSampler() {
		this.algorithm = new SimulatedAnnealing();
	}
	
	@Override
	public List<APopulationEntity> draw(int numberOfDraw) {
		return new ArrayList<>(this.algorithm.run(new GSSAState(
				basicSampler.draw(numberOfDraw), sample)).getSolution());
	}
	
	@Override
	public Set<APopulationEntity> drawUnique(int numberOfDraw) {
		return new HashSet<>(this.algorithm.run(new GSSAState(
				basicSampler.drawUnique(numberOfDraw), sample)).getSolution());
	}

}
