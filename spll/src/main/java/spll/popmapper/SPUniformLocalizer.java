package spll.popmapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Geometry;

import core.io.geo.IGSGeofile;
import core.io.geo.entity.AGeoEntity;
import core.io.survey.entity.AGenstarEntity;
import core.io.survey.entity.attribut.AGenstarAttribute;
import core.io.survey.entity.attribut.value.AGenstarValue;
import core.metamodel.IPopulation;
import spll.popmapper.constraint.SpatialConstraint;
import spll.popmapper.pointInalgo.PointInLocalizer;
import spll.popmapper.pointInalgo.RandomPointInLocalizer;


public class SPUniformLocalizer implements ISPLocalizer {

	
	private IGSGeofile match; //main referenced area for placing the agents (ex: Iris)
	private IGSGeofile localisation; //possible nest for agents (ex: buildings)
	private IGSGeofile entityNbAreas; //gives the number of entities per area (ex: regression cells)
	private List<SpatialConstraint> constraints; //spatial constraints related to the placement of the entities in their nest
	private Random rand;  
	private PointInLocalizer pointInLocalizer; //allows to return one or several points in a geometry
	
	private String numberProperty; //name of the attribute that contains the number of entities in the entityNbAreas file
	private String keyAttPop; //name of the attribute that is used to store the id of the referenced area  in the population
	private String keyAttMatch; //name of the attribute that is used to store the id of the referenced area in the entityNbAreas file
	
	public SPUniformLocalizer(IGSGeofile localisation) {
		this.localisation = localisation;
		rand = new Random();
		pointInLocalizer = new RandomPointInLocalizer(rand);
	}
	
	@Override
	public IPopulation<AGenstarEntity, AGenstarAttribute, AGenstarValue> localisePopulation(
			IPopulation<AGenstarEntity, AGenstarAttribute, AGenstarValue> population) {
		//define the crs of the population
		population.setCrs(localisation.getCoordRefSystem());
		try {
			//case where the referenced file is not defined
			if (match == null) {
				List<AGenstarEntity> entities = new ArrayList<>(population);
				
				//case where there is no information about the number of entities in specific spatial areas
				if (numberProperty == null || entityNbAreas == null) {
					randomLocalizationInNest(entities, null);
				}
				//case where we have information about the number of entities per specific areas (entityNbAreas)
				else {
					randomLocalizationInNestWithNumbers(entities, null);
				}
			}
			//case where the referenced file is defined
			else {
				for (AGeoEntity globalfeature : match.getGeoData()) {
					String valKeyAtt = globalfeature.getValueForAttribute(keyAttMatch).getStringValue();
					List<AGenstarEntity> entities = population.stream()
						.filter(s -> s.getValueForAttribute(keyAttPop).getStringValue().equals(valKeyAtt))
						.collect(Collectors.toList());
					if (numberProperty == null || entityNbAreas == null) {
						randomLocalizationInNest(entities, globalfeature.getGeometry());
					}
					else {
						randomLocalizationInNestWithNumbers(entities, globalfeature.getGeometry());
					}
				}
			} 
				
		} catch (IOException | TransformException e) {
			e.printStackTrace();
		} 
		return population;
	}

	//set to all the entities given as argument, a given nest chosen randomly in the possible geoEntities 
	//of the localisation shapefile (all if not bounds is defined, only the one in the bounds if the one is not null)
	private void randomLocalizationInNest(Collection<AGenstarEntity> entities, Geometry spatialBounds) throws IOException, TransformException {
		Object[] locTab = spatialBounds == null ? localisation.getGeoData().toArray() : localisation.getGeoDataWithin(spatialBounds).toArray();
		int nb = locTab.length;
		for (AGenstarEntity entity : entities) {
			AGeoEntity nest = (AGeoEntity) locTab[rand.nextInt(nb)];
			entity.setNest(nest);
			entity.setLocation(pointInLocalizer.pointIn(nest.getGeometry()));
		}
	}
	
	// For each area concerned of the entityNbAreas shapefile  (all if not bounds is defined, only the one in the bounds if the one is not null),
	//define the number of entities from the entities list to locate inside, then try to set a nest to this randomly chosen number of entities.
	// NOTE: if no nest is located inside the area, not entities will be located inside.
	private void randomLocalizationInNestWithNumbers(List<AGenstarEntity> entities, Geometry spatialBounds) 
			throws IOException, TransformException {
		Collection<? extends AGeoEntity> areas = spatialBounds == null ? 
				entityNbAreas.getGeoData() : entityNbAreas.getGeoDataWithin(spatialBounds);
		for (AGeoEntity feature: areas) {
			Object[] locTab = null;
			if (localisation == entityNbAreas) {
				locTab = new Object[1];
				locTab[0] = feature;
			} else {
				locTab = localisation.getGeoDataWithin(feature.getGeometry()).toArray();
			}
			int nb = locTab.length;
			if (nb == 0) continue;
			double val = feature.getValueForAttribute(numberProperty).getNumericalValue().doubleValue();
			for (int i = 0; i < val; i++) {
				if (entities.isEmpty()) break;
				int index = rand.nextInt(entities.size());
				AGenstarEntity entity = entities.remove(index);
				
				AGeoEntity nest = (AGeoEntity) locTab[rand.nextInt(nb)];
				entity.setNest(nest);
				entity.setLocation(pointInLocalizer.pointIn(nest.getGeometry()));
			}
		}
	}
		
	
	
	

	public void setMatch(IGSGeofile match, String keyAttPop, String keyAttMatch) {
		this.match = match;
		this.keyAttPop = keyAttPop;
		this.keyAttMatch = keyAttMatch;
	}

	public void setLocalisation(IGSGeofile localisation) {
		this.localisation = localisation;
	}
	public void setEntityNbAreas(IGSGeofile entityNbAreas, String numberProperty) {
		this.entityNbAreas = entityNbAreas;
		this.numberProperty = numberProperty;
	}

	public List<SpatialConstraint> getConstraints() {
		return constraints;
	}
	public void setConstraints(List<SpatialConstraint> constraints) {
		this.constraints = constraints;
	}

	public Random getRand() {
		return rand;
	}

	public void setRand(Random rand) {
		this.rand = rand;
		pointInLocalizer.setRand(rand);
	}

	public PointInLocalizer getPointInLocalizer() {
		return pointInLocalizer;
	}

	public void setPointInLocalizer(PointInLocalizer pointInLocalizer) {
		this.pointInLocalizer = pointInLocalizer;
	}
	
	
}
