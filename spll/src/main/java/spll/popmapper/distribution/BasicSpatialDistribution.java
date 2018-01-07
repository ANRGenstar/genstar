package spll.popmapper.distribution;

import java.util.List;
import java.util.stream.Collectors;

import core.metamodel.entity.ADemoEntity;
import core.metamodel.entity.AGeoEntity;
import core.metamodel.value.IValue;
import core.util.random.roulette.RouletteWheelSelectionFactory;
import spll.popmapper.distribution.function.ISpatialEntityFunction;

/**
 * Spatial Distribution that relies on spatial entity attribute to asses probability. For exemple,
 * probability could be computed based on the area of spatial entity.
 * 
 * @author kevinchapuis
 *
 * @param <N>
 */
public class BasicSpatialDistribution<N extends Number, E extends ADemoEntity> implements ISpatialDistribution<E> {
	
	private ISpatialEntityFunction<N> function;

	public BasicSpatialDistribution(ISpatialEntityFunction<N> function) {
		this.function = function;
	}
	
	@Override
	public AGeoEntity<? extends IValue> getCandidate(E entity, List<? extends AGeoEntity<? extends IValue>> candidates) {
		return RouletteWheelSelectionFactory.getRouletteWheel(candidates.stream()
				.map(a -> function.apply(a)).collect(Collectors.toList()), candidates)
			.drawObject();
	}
	
}
