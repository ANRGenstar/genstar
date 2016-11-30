package spll.popmapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.commons.collections.IteratorUtils;
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
import core.io.geo.ShapeFile;
import core.io.geo.entity.AGeoEntity;
import core.io.geo.entity.attribute.AGeoAttribute;
import core.metamodel.IEntity;
import core.metamodel.IPopulation;
public class SPUniformLocalizer implements ISPLocalizer {

	@SuppressWarnings("rawtypes")
	private IPopulation<IEntity,?,?> population;
	private ShapeFile match;
	private IGSGeofile localisation;
	private String numberProperty;
	private Random rand;
	private String keyAttPop;
	private String keyAttMatch;
	
	public static GeometryFactory FACTORY = new GeometryFactory();
	
	public SPUniformLocalizer(IPopulation<IEntity,?,?> population, ShapeFile match, IGSGeofile localisation, String numberProperty, String keyAttPop, String keyAttMatch ) {
		this.population = population;
		this.match = match;
		this.localisation = localisation;
		this.numberProperty = numberProperty;
		this.keyAttPop = keyAttPop;
		this.keyAttMatch = keyAttMatch;
		rand = new Random();
	}
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public IPopulation localisePopulation() {
		// TODO Auto-generated method stub
		
		// TODO: how to match attribute feature and attribute individual, both are IEntity
		
		// TODO: for each feature, randomly spread individual with a fitness (reproduce in each cells the feature distribution)
		population.setCrs(localisation.getCoordRefSystem());
		try {
			if (match != null) {
				for (AGeoEntity globalfeature : match.getGeoData()) {
					String valKeyAtt = globalfeature.getValueForAttribute(keyAttMatch).getStringValue();
					List<IEntity> entities = population.stream().filter(s -> s.getValueForAttribute(keyAttPop).toString().equals(valKeyAtt)).collect(Collectors.toList());
					if (numberProperty == null) {
						Object[] locTab = IteratorUtils.toArray(localisation.getGeoAttributeIteratorWithin(globalfeature.getGeometry()));
						int nb = locTab.length;
						for (IEntity entity : population) {
							AGeoEntity feature = (AGeoEntity) locTab[rand.nextInt(nb)];
							entity.setNest(feature);
							entity.setLocation(pointInGeom(feature.getGeometry(),rand));
						}
					} else {
						Iterator<? extends AGeoEntity> itr = localisation.getGeoAttributeIteratorWithin(globalfeature.getGeometry());
						while(itr.hasNext()) {
							AGeoEntity feature = itr.next();
							List<String> atts = new ArrayList();
							for (AGeoAttribute at: feature.getAttributes())atts.add(at.getAttributeName());
							double val = feature.getValueForAttribute(numberProperty).getNumericalValue().doubleValue();
							for (int i = 0; i < val; i++) {
								if (entities.isEmpty()) break;
								int index = rand.nextInt(entities.size());
								IEntity entity = (IEntity) entities.remove(index);
								entity.setNest(feature);
								entity.setLocation(pointInGeom(feature.getGeometry(),rand));
							}
						}
					}
				}
			} else {
				List<IEntity> entities = new ArrayList<IEntity>(population);
				if (numberProperty == null) {
					Object[] locTab = localisation.getGeoData().toArray();
					int nb = locTab.length;
					for (IEntity entity : population) {
						AGeoEntity feature = (AGeoEntity) locTab[rand.nextInt(nb)];
						entity.setNest(feature);
						entity.setLocation(pointInGeom(feature.getGeometry(),rand));
					}
				} else {
					for (AGeoEntity feature: localisation.getGeoData()) {
						List<String> atts = new ArrayList();
						for (AGeoAttribute at: feature.getAttributes())atts.add(at.getAttributeName());
						double val = feature.getValueForAttribute(numberProperty).getNumericalValue().doubleValue();
						for (int i = 0; i < val; i++) {
							if (entities.isEmpty()) break;
							int index = rand.nextInt(entities.size());
							IEntity entity = (IEntity) entities.remove(index);
							entity.setNest(feature);
							entity.setLocation(pointInGeom(feature.getGeometry(),rand));
						}
					}
				}
				
			}
		} catch (IOException | TransformException e) {
			e.printStackTrace();
		} 
		return population;
	}
	
	public static Point pointInGeom(final Geometry geom, final Random rand) {
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
	
	
	public static Integer opRndChoice(final List<Double> distribution, final Random rand) {
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


}
