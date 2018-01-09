package spll.popmapper.distribution;

import java.util.List;

import core.metamodel.entity.ADemoEntity;
import core.metamodel.entity.AGeoEntity;
import core.metamodel.value.IValue;
import core.util.random.GenstarRandom;

/**
 * Spatial Distribution that relies on spatial entity attribute to asses probability. For exemple,
 * probability could be computed based on the area of spatial entity.
 * 
 * @author kevinchapuis
 *
 * @param <N>
 */
public class UniformSpatialDistribution<N extends Number, E extends ADemoEntity> implements ISpatialDistribution<E> {
	
	
	
	@Override
	public AGeoEntity<? extends IValue> getCandidate(E entity, List<? extends AGeoEntity<? extends IValue>> candidates) {
		return candidates.get(GenstarRandom.getInstance().nextInt(candidates.size()));
	}
	
}
