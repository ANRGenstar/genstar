package spll.popmapper.distribution;

import java.util.List;
import java.util.stream.Collectors;

import core.metamodel.entity.AGeoEntity;
import core.metamodel.value.IValue;
import core.util.random.roulette.RouletteWheelSelectionFactory;
import spll.SpllEntity;

public class DistanceSpatialDistribution implements ISpatialDistribution {
	
	@Override
	public AGeoEntity<? extends IValue> getCandidate(SpllEntity entity, List<AGeoEntity<? extends IValue>> candidates) {
		return candidates.get(RouletteWheelSelectionFactory.getRouletteWheel(candidates.stream()
				.map(a -> a.getGeometry().distance(entity.getLocation())).collect(Collectors.toList()))
			.drawIndex());
	}

}
