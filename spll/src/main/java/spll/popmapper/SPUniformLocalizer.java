package spll.popmapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import core.metamodel.geo.AGeoEntity;
import core.metamodel.pop.APopulationEntity;
import spll.SpllPopulation;
import spll.popmapper.constraint.SpatialConstraint;


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
	protected List localizationInNestOp(Collection<APopulationEntity> entities,
			List<AGeoEntity> possibleNests, Long val) {
		Collection<APopulationEntity> chosenEntities = null;
		if (val != null) {
			List<APopulationEntity> ens = new ArrayList<>(entities);
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
		for (APopulationEntity entity : chosenEntities) {
			if (possibleNests.isEmpty()) {
				break;
			}
			int index = rand.nextInt(possibleNests.size());
			AGeoEntity nest = (AGeoEntity) possibleNests.get(index);
			boolean removeObject = false;
			for (SpatialConstraint constraint: constraints) {
				removeObject = removeObject || constraint.updateConstraint(entity, nest);
			}
			if (removeObject) possibleNests.remove(index);
			entity.setNest(nest);
			entity.setLocation(pointInLocalizer.pointIn(nest.getGeometry()));
			
		}
		return entities.stream().filter(a -> a.getLocation() == null).collect(Collectors.toList());
	}
}

	
	/*@Override
	protected List<APopulationEntity> localizationInNestOp(Collection<APopulationEntity> entities, Geometry spatialBounds) {
		ArrayList<AGeoEntity> locTab = null;
		try {
			locTab = spatialBounds == null ? new ArrayList<>(population.getGeography().getGeoEntity()) :  new ArrayList<>(population.getGeography().getGeoEntityWithin(spatialBounds));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		ArrayList<AGeoEntity> locTabInit = null;
		if (maxPerNest != null) {
			locTab = (ArrayList<AGeoEntity>) locTab.stream().filter(a -> maxPerNest.get(a.getGenstarName()) > 0).collect(Collectors.toList());
			if (constraintRelaxation == SpatialConstraintRelaxationType.MaxPerNest) {locTabInit = new ArrayList<>(locTab); }
		}
		int nb = locTab.size();
		if (nb == 0) return null;
		for (APopulationEntity entity : entities) {
			int index = rand.nextInt(nb);
			AGeoEntity nest = (AGeoEntity) locTab.get(index);
			if (maxPerNest != null) {
				Integer maxNb = maxPerNest.get(nest.getGenstarName());
				if (maxNb == 1) {
					locTab.remove(index);
					nb --;
				} 
				maxPerNest.put(nest.getGenstarName(), maxNb - 1);
			}
			entity.setNest(nest);
			entity.setLocation(pointInLocalizer.pointIn(nest.getGeometry()));
			if (nb == 0) {
				if (constraintRelaxation == SpatialConstraintRelaxationType.NumberEntities)
					return null;
				else if (constraintRelaxation == SpatialConstraintRelaxationType.MaxPerNest) {
					for (AGeoEntity e : locTabInit) {
						maxPerNest.put(e.getGenstarName(), 1);
					}
					locTab = new ArrayList<>(locTabInit);
					nb = locTab.size();
				}
			}
		}
		return entities.stream().filter(a -> a.getLocation() != null).collect(Collectors.toList());
	}

	@Override
	protected List<APopulationEntity> localizationInNestWithNumbersOp(Collection<APopulationEntity> entities,
			Geometry spatialBounds){
			Collection<? extends AGeoEntity> areas = spatialBounds == null ? 
				map.getGeoEntity() : map.getGeoEntityWithin(spatialBounds);
				Map<String,Double> vals = map.getGeoEntity().stream().collect(Collectors.toMap(a -> ((AGeoEntity)a).getGenstarName(), a -> a.getValueForAttribute(keyAttMap).getNumericalValue().doubleValue()));
				
				if (map.getGeoGSFileType().equals(GeoGSFileType.RASTER)) {
					double unknowVal = ((SPLRasterFile) map).getNoDataValue();
					List<String> es = new ArrayList<>(vals.keySet());
					for (String e : es) {
						if (vals.get(e).doubleValue() == unknowVal) {
							vals.remove(e);
						}
					}
				}
				Double tot = vals.values().stream().mapToDouble(s -> s).sum();
				if (tot == 0) return;
				for (AGeoEntity feature: areas) {
					ArrayList<AGeoEntity> locTab = null;
					ArrayList<AGeoEntity> locTabInit = null;
					
					if (map.getGeoGSFileType().equals(GeoGSFileType.RASTER))  {
						if (!vals.containsKey(feature.getGenstarName())) continue;
					}
					if (population.getGeography() == map) {
						locTab = new ArrayList<>();
						locTab.add(feature);
					} else {
						locTab = new ArrayList<>(population.getGeography().getGeoEntityWithin(feature.getGeometry()));
					}
					if (maxPerNest != null) {
						locTab = (ArrayList<AGeoEntity>) locTab.stream().filter(a -> maxPerNest.get(a.getGenstarName()) > 0).collect(Collectors.toList());
						if (constraintRelaxation == SpatialConstraintRelaxationType.MaxPerNest) {locTabInit = new ArrayList<>(locTab); }
						
					}
					int nb = locTab.size();
					if (nb == 0) continue;
					
					long val = Math.round(population.size() *vals.get(feature.getGenstarName()) / tot);
					
					for (int i = 0; i < val; i++) {
						if (entities.isEmpty()) break;
						int index = rand.nextInt(entities.size());
						APopulationEntity entity = entities.remove(index);
						int indexNest = rand.nextInt(nb);
						AGeoEntity nest = locTab.get(indexNest);
						if (maxPerNest != null) {
							Integer maxNb = maxPerNest.get(nest.getGenstarName());
							if (maxNb == null || maxNb == 1) {
								locTab.remove(index);
								nb --;
							}
							maxPerNest.put(nest.getGenstarName(), maxNb - 1);
						}
						entity.setNest(nest);
						entity.setLocation(pointInLocalizer.pointIn(nest.getGeometry()));
						if (nb == 0)  {
							if (constraintRelaxation == SpatialConstraintRelaxationType.NumberEntities)
								break;
							if (constraintRelaxation == SpatialConstraintRelaxationType.MaxPerNest) {
								for (AGeoEntity e : locTabInit) {
									maxPerNest.put(e.getGenstarName(), 1);
									
								}
								locTab = new ArrayList<>(locTabInit);
								nb = locTab.size();
							}
						}
					}
				}
	}



*/
