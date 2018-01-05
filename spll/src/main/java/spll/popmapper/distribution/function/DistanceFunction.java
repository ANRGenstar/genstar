package spll.popmapper.distribution.function;

import java.util.Collection;

import core.metamodel.entity.AGeoEntity;
import core.metamodel.value.IValue;
import spll.SpllEntity;

public class DistanceFunction implements ISpatialComplexFunction<Double> {

	@Override
	public Double apply(AGeoEntity<? extends IValue> spatialEntity, SpllEntity entity) {
		return spatialEntity.getGeometry().distance(entity.getLocation());
	}

	@Override
	public void updateFunctionState(Collection<SpllEntity> entities,
			Collection<AGeoEntity<? extends IValue>> candidates) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

}
