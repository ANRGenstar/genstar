package spll.popmapper.distribution;

import java.util.List;

import core.metamodel.entity.AGeoEntity;
import core.metamodel.value.IValue;
import core.util.random.GenstarRandom;
import spll.SpllEntity;

public class UniformSpatialDistribution implements ISpatialDistribution{

	@Override
	public AGeoEntity<? extends IValue> getCandidate(SpllEntity entity, List<AGeoEntity<? extends IValue>> candidates) {
		return candidates.get(GenstarRandom.getInstance().nextInt(candidates.size()));
	}
	
}
