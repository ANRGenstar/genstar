package spll.popmapper.distribution;

import java.util.List;

import core.metamodel.entity.AGeoEntity;
import core.metamodel.value.IValue;
import spll.SpllEntity;

public interface ISpatialDistribution {

	public AGeoEntity<? extends IValue> getCandidate(SpllEntity entity, List<AGeoEntity<? extends IValue>> candidates);
	
	public void releaseCache();
}
