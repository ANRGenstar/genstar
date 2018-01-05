package spll.popmapper.distribution;

import java.util.List;
import java.util.stream.Collectors;

import core.metamodel.entity.AGeoEntity;
import core.metamodel.value.IValue;
import core.util.random.roulette.RouletteWheelSelectionFactory;
import spll.SpllEntity;
import spll.popmapper.distribution.function.ISpatialComplexFunction;

/**
 * Spatial distribution that relies on both attribute of spatial and population entity. 
 * For example, probability attached to the distance between the entity to bind and the spatial entity to be bound with.
 * 
 * @author kevinchapuis
 *
 * @param <N>
 */
public class ComplexSpatialDistribution<N extends Number> implements ISpatialDistribution<SpllEntity> {

	private ISpatialComplexFunction<N> function;
	
	public ComplexSpatialDistribution(ISpatialComplexFunction<N> function) {
		this.function = function;
	}
	
	@Override
	public AGeoEntity<? extends IValue> getCandidate(SpllEntity entity, List<AGeoEntity<? extends IValue>> candidates) {
		return RouletteWheelSelectionFactory.getRouletteWheel(candidates.stream()
				.map(candidate -> function.apply(candidate, entity)).collect(Collectors.toList()), candidates)
			.drawObject();
	}

}
