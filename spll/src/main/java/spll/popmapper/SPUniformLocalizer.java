package spll.popmapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.precision.GeometryPrecisionReducer;

import core.io.geo.IGSGeofile;
import core.io.geo.entity.AGeoEntity;
import core.metamodel.IAttribute;
import core.metamodel.IEntity;
import core.metamodel.IPopulation;
import core.metamodel.IValue;
import spll.constraint.SpatialConstraint;
public class SPUniformLocalizer implements ISPLocalizer {

	@SuppressWarnings("rawtypes")
	private IGSGeofile match; //main referenced area for placing the agents (ex: Iris)
	private IGSGeofile localisation; //possible nest for agents (ex: buildings)
	private IGSGeofile entityNbAreas; //gives the number of entities per area (ex: regression cells)
	private List<SpatialConstraint> constraints; //spatial constraints related to the placement of the entities in their nest
	private Random rand;  
	
	private String numberProperty; //name of the attribute that contains the number of entities in the entityNbAreas file
	private String keyAttPop; //name of the attribute that is used to store the id of the referenced area  in the population
	private String keyAttMatch; //name of the attribute that is used to store the id of the referenced area in the entityNbAreas file
	
	public static GeometryFactory FACTORY = new GeometryFactory();
	
	public SPUniformLocalizer(IGSGeofile localisation) {
		this.localisation = localisation;
		rand = new Random();
	}
	
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public IPopulation<IEntity<IAttribute<IValue>, IValue>, IAttribute<IValue>, IValue> localisePopulation(IPopulation<IEntity<IAttribute<IValue>, IValue>, IAttribute<IValue>, IValue> population) {
		// TODO Auto-generated method stub
		
		// TODO: how to match attribute feature and attribute individual, both are IEntity
		
		// TODO: for each feature, randomly spread individual with a fitness (reproduce in each cells the feature distribution)
		population.setCrs(localisation.getCoordRefSystem());
		try {
			//case where the referenced file is not defined
			if (match == null) {
				List<IEntity> entities = new ArrayList<IEntity>(population);
				
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
					List<IEntity> entities = population.stream().filter(s -> s.getValueForAttribute(keyAttPop).toString().equals(valKeyAtt)).collect(Collectors.toList());
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

	private void randomLocalizationInNest(Collection<IEntity> entities, Geometry spatialBounds) throws IOException, TransformException {
		Object[] locTab = spatialBounds == null ? localisation.getGeoData().toArray() : localisation.getGeoDataWithin(spatialBounds).toArray();
		int nb = locTab.length;
		for (IEntity entity : entities) {
			AGeoEntity nest = (AGeoEntity) locTab[rand.nextInt(nb)];
			entity.setNest(nest);
			entity.setLocation(pointInGeom(nest.getGeometry(),rand));
		}
	}
	
	private void randomLocalizationInNestWithNumbers(List<IEntity> entities, Geometry spatialBounds) throws IOException, TransformException {
		Collection<? extends AGeoEntity> areas = spatialBounds == null ? entityNbAreas.getGeoData() : entityNbAreas.getGeoDataWithin(spatialBounds);
		for (AGeoEntity feature: areas) {
			Object[] locTab = localisation.getGeoDataWithin(feature.getGeometry()).toArray();
			int nb = locTab.length;
			if (nb == 0) continue;
			double val = feature.getValueForAttribute(numberProperty).getNumericalValue().doubleValue();
			for (int i = 0; i < val; i++) {
				if (entities.isEmpty()) break;
				int index = rand.nextInt(entities.size());
				IEntity entity = entities.remove(index);
				
				AGeoEntity nest = (AGeoEntity) locTab[rand.nextInt(nb)];
				entity.setNest(nest);
				entity.setLocation(pointInGeom(nest.getGeometry(),rand));
				
				
			}
		}
	}
		
	private static Point pointInGeom(final Geometry geom, final Random rand) {
		GeometryFactory fact = new GeometryFactory();
		if (geom == null || geom.getCoordinate() == null) {
			return null;
		}

		if (geom instanceof Point || geom.getCoordinates().length < 2) {
			return fact.createPoint(geom.getCoordinate());
		}
		if (geom instanceof LineString) {
			double perimeter = geom.getLength();
			double dist = perimeter * rand.nextDouble();
			double sumDist = 0;
			Coordinate pS = ((LineString) geom).getCoordinateN(0);
			for (int i = 1; i < geom.getNumPoints(); i++) {
				Coordinate pT = ((LineString) geom).getCoordinateN(i);
				double d = pS.distance(pT);
				if ((d + sumDist) >= dist) {
					double ratio = (dist - sumDist)/d;
					final double newX = pS.x + ratio * (pT.x - pS.x);
					final double newY = pS.y + ratio * (pT.y - pS.y);
					final double newZ = pS.z + ratio * (pT.z - pS.z);
					return fact.createPoint(new Coordinate(newX, newY, newZ)); 
				}
				pS = pT;
				sumDist += d;
			}
		}
		if (geom instanceof Polygon) {
			if (geom.getArea() > 0) {
				final Envelope env = geom.getEnvelopeInternal();
				final double xMin = env.getMinX();
				final double xMax = env.getMaxX();
				final double yMin = env.getMinY();
				final double yMax = env.getMaxY();
				double newX = xMin + rand.nextDouble() * (xMax - xMin);
				double newY= yMin + rand.nextDouble() * (yMax - yMin);
				Point pt = fact.createPoint(new Coordinate(newX, newY)); 
				while (!geom.intersects(pt)) {
					newX = xMin + rand.nextDouble() * (xMax - xMin);
					newY= yMin + rand.nextDouble() * (yMax - yMin);
					pt = fact.createPoint(new Coordinate(newX, newY)); 
				}
				return pt;
			}
			final Envelope env = geom.getEnvelopeInternal();
			final double xMin = env.getMinX();
			final double xMax = env.getMaxX();
			final double yMin = env.getMinY();
			final double yMax = env.getMaxY();
			final double x = xMin + rand.nextDouble() * (xMax - xMin);
			final Coordinate coord1 = new Coordinate(x, yMin);
			final Coordinate coord2 = new Coordinate(x, yMax);
			final Coordinate[] coords = { coord1, coord2 };
			Geometry line = FACTORY.createLineString(coords);
			try {
				line = line.intersection(geom);
			} catch (final Exception e) {
				try {final PrecisionModel pm = new PrecisionModel(PrecisionModel.FLOATING_SINGLE);
				line = GeometryPrecisionReducer.reducePointwise(line, pm)
						.intersection(GeometryPrecisionReducer.reducePointwise(geom, pm));
				}catch (final Exception e5) {
					line = line
							.intersection(geom.buffer(0.1));
				}
			} 
			return pointInGeom((line), rand);
		}
		if (geom instanceof GeometryCollection) {
			if (geom instanceof MultiLineString) {
				List<Double> distribution = new ArrayList<Double>();
				for (int i = 0; i < geom.getNumGeometries(); i++) {
					distribution.add((geom.getGeometryN(i)).getLength());
				}
				int index = opRndChoice(distribution, rand);
				return pointInGeom((geom.getGeometryN(index)), rand);
			} else if (geom instanceof MultiPolygon) {
				List<Double> distribution = new ArrayList<Double>();
				for (int i = 0; i < geom.getNumGeometries(); i++) {
					distribution.add((geom.getGeometryN(i)).getArea());
				}
				int index = opRndChoice(distribution, rand);
				return pointInGeom((geom.getGeometryN(index)), rand);
			} 
			return pointInGeom((geom.getGeometryN(rand.nextInt(geom.getNumGeometries()))), rand);
		}

		return null;

	}
	
	
	private static Integer opRndChoice(final List<Double> distribution, final Random rand) {
		Double sumElt = 0.0;
		List<Double> normalizedDistribution = new ArrayList<Double>();
		for (final Double eltDistrib : distribution) {
			normalizedDistribution.add(eltDistrib);
			sumElt += eltDistrib;
		}
		

		for (int i = 0; i < normalizedDistribution.size(); i++) {
			normalizedDistribution.set(i, normalizedDistribution.get(i) / sumElt);
		}

		double randomValue = rand.nextDouble();

		for (int i = 0; i < distribution.size(); i++) {
			randomValue = randomValue - normalizedDistribution.get(i);
			if (randomValue <= 0) {
				return i;
			}
		}
		return -1;
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
	}
}
