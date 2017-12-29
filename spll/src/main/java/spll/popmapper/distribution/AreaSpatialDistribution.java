package spll.popmapper.distribution;

import java.util.List;
import java.util.stream.Collectors;

import core.metamodel.entity.AGeoEntity;
import core.metamodel.value.IValue;
import core.util.random.GenstarRandom;
import spll.SpllEntity;

public class AreaSpatialDistribution extends AbstractSpatialDistribution{

	List<Double> distribution = null;
	
	@Override
	public AGeoEntity<? extends IValue> getCandidate(SpllEntity entity, List<AGeoEntity<? extends IValue>> candidates) {
		if (distribution == null) {
			distribution = candidates.stream().map(a -> a.getArea()).collect(Collectors.toList());
			distribution = normalizeDistribution(distribution);
		}
		
		int index = randomChoice(distribution);
		if (index == -1) {
			candidates.get(GenstarRandom.getInstance().nextInt(candidates.size()));
		}
		return candidates.get(index);
	}

	@Override
	public void releaseCache() {
		distribution = null;
	}

	
}
