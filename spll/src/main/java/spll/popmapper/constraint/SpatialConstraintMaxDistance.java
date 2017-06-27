package spll.popmapper.constraint;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import core.metamodel.geo.AGeoEntity;

public class SpatialConstraintMaxDistance extends ASpatialConstraint {

	private Map<AGeoEntity, Double> distanceToEntities;

	public SpatialConstraintMaxDistance(Collection<AGeoEntity> distanceToEntities,
			Double distance) {
		this.distanceToEntities = distanceToEntities.stream().collect(Collectors
				.toMap(Function.identity(), entity -> distance));
	}
	
	public SpatialConstraintMaxDistance(Map<AGeoEntity, Double> distanceToEntities) {
		this.distanceToEntities = distanceToEntities;
	}
	
	@Override
	public List<AGeoEntity> getSortedCandidates(List<AGeoEntity> nests) {
		return nests.stream().filter(nest -> distanceToEntities.keySet()
				.stream().anyMatch(entity -> nest.getGeometry()
						.getCentroid().buffer(distanceToEntities.get(entity))
						.intersects(entity.getGeometry())))
				.sorted((c1, c2) -> Double.compare(
						distanceToEntities.keySet().stream().mapToDouble(entity -> c1.getGeometry().getCentroid()
								.distance(entity.getGeometry())).min().getAsDouble(),
						distanceToEntities.keySet().stream().mapToDouble(entity -> c2.getGeometry().getCentroid()
								.distance(entity.getGeometry())).min().getAsDouble()))
				.collect(Collectors.toList());
	}

	@Override
	public boolean updateConstraint(AGeoEntity nest) {
		return false;
	}

	@Override
	public void relaxConstraintOp(Collection<AGeoEntity> distanceToEntities) {
		distanceToEntities.stream().forEach(entity -> 
			this.distanceToEntities.put(entity, 
					this.distanceToEntities.get(entity)+this.increaseStep));

	}

}
