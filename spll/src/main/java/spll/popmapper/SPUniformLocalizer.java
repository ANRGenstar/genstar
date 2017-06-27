package spll.popmapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import core.metamodel.geo.AGeoEntity;
import spll.SpllPopulation;
import spll.SpllPopulationEntity;
import spll.popmapper.constraint.ISpatialConstraint;

public class SPUniformLocalizer extends AbstratcLocalizer {

	
	/**
	 * Build a localizer based on a geographically grounded population
	 *  
	 * @param population
	 */
	public SPUniformLocalizer(SpllPopulation population) {
		super(population);
	}

	@Override
	protected List<SpllPopulationEntity> localizationInNestOp(Collection<SpllPopulationEntity> entities,
			List<AGeoEntity> possibleNests, Long val) {
		Collection<SpllPopulationEntity> chosenEntities = null;
		if (val != null) {
			List<SpllPopulationEntity> ens = new ArrayList<>(entities);
			chosenEntities = new ArrayList<>();
			val = Math.min(val, ens.size());
			for (int i = 0; i < val; i++) {
				int index = rand.nextInt(ens.size());
				chosenEntities.add(ens.get(index));
				ens.remove(index);
			}
		}else {
			chosenEntities = entities;
		}
		for (SpllPopulationEntity entity : chosenEntities) {
			if (possibleNests.isEmpty()) {
				break;
			}
			int index = rand.nextInt(possibleNests.size());
			AGeoEntity nest = possibleNests.get(index);
			boolean removeObject = false;
			for (ISpatialConstraint constraint: constraints) {
				removeObject = removeObject || constraint.updateConstraint(nest);
			}
			if (removeObject) possibleNests.remove(index);
			entity.setNest(nest);
			entity.setLocation(pointInLocalizer.pointIn(nest.getGeometry()));
			
		}
		return entities.stream().filter(a -> a.getLocation() == null)
				.collect(Collectors.toList());
	}
}

