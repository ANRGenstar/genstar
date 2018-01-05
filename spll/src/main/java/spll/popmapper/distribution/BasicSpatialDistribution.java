package spll.popmapper.distribution;

import java.util.List;
import java.util.stream.Collectors;

import core.metamodel.entity.ADemoEntity;
import core.metamodel.entity.AGeoEntity;
import core.metamodel.value.IValue;
import core.util.random.roulette.RouletteWheelSelectionFactory;
import spll.popmapper.distribution.function.ISpatialEntityToNumber;

/**
 * Spatial Distribution that relies on spatial entity attribute to asses probability. For exemple,
 * probability could be computed based on the area of spatial entity.
 * 
 * @author kevinchapuis
 *
 * @param <N>
 */
public class BasicSpatialDistribution<N extends Number> implements ISpatialDistribution<ADemoEntity> {
	
	private ISpatialEntityToNumber<N> function;

	public BasicSpatialDistribution(ISpatialEntityToNumber<N> function) {
		this.function = function;
	}
	
	@Override
	public AGeoEntity<? extends IValue> getCandidate(ADemoEntity entity, List<AGeoEntity<? extends IValue>> candidates) {
		return RouletteWheelSelectionFactory.getRouletteWheel(candidates.stream()
				.map(a -> function.apply(a)).collect(Collectors.toList()), candidates)
			.drawObject();
	}
	
}
