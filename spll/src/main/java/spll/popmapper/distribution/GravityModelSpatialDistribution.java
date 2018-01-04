package spll.popmapper.distribution;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import core.metamodel.entity.AGeoEntity;
import core.metamodel.value.IValue;
import core.util.random.roulette.RouletteWheelSelectionFactory;
import spll.SpllEntity;

public class GravityModelSpatialDistribution implements ISpatialDistribution {

	Map<AGeoEntity<? extends IValue>, Double> mass; 
	
	/**
	 * Mass of spatial entity is defined as the sum of distance between the spatial entity and all entities
	 * 
	 * @param candidates
	 * @param entities
	 */
	public GravityModelSpatialDistribution(Collection<AGeoEntity<? extends IValue>> candidates, SpllEntity... entities) {
		this.mass = candidates.stream().collect(Collectors.toMap(Function.identity(), se -> Arrays.asList(entities).stream()
				.mapToDouble(e -> se.getGeometry().distance(e.getLocation())).sum()));
	}
	
	/**
	 * Mass of spatial entity is defined as the number of entity within a given buffer around the spatial entity
	 * 
	 * @param candidates
	 * @param buffer
	 * @param entities
	 */
	public GravityModelSpatialDistribution(Collection<AGeoEntity<? extends IValue>> candidates, 
			double buffer, SpllEntity... entities) {
		this.mass = candidates.stream().collect(Collectors.toMap(Function.identity(), spacEntity -> (double) Arrays.asList(entities).stream()
				.filter(e -> spacEntity.getGeometry().buffer(buffer).contains(e.getLocation())).count()));
	}
	
	@Override
	public AGeoEntity<? extends IValue> getCandidate(SpllEntity entity, List<AGeoEntity<? extends IValue>> candidates) {
		return RouletteWheelSelectionFactory.getRouletteWheel(candidates.stream()
				.map(se -> mass.get(se) / se.getGeometry().distance(entity.getLocation())).collect(Collectors.toList()), candidates)
				.drawObject();
	}

}
