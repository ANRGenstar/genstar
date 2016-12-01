package spll.popmapper.pointInalgo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

public class RandomPointInLocalizer implements PointInLocalizer{

	private Random rand;
	
	public static GeometryFactory FACTORY = new GeometryFactory();
	
	
	public RandomPointInLocalizer(Random rand) {
		super();
		this.rand = rand;
	}

	

	@Override
	public Point pointIn(Geometry geom) {
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
			return pointIn((line));
		}
		if (geom instanceof GeometryCollection) {
			if (geom instanceof MultiLineString) {
				List<Double> distribution = new ArrayList<Double>();
				for (int i = 0; i < geom.getNumGeometries(); i++) {
					distribution.add((geom.getGeometryN(i)).getLength());
				}
				int index = opRndChoice(distribution);
				return pointIn((geom.getGeometryN(index)));
			} else if (geom instanceof MultiPolygon) {
				List<Double> distribution = new ArrayList<Double>();
				for (int i = 0; i < geom.getNumGeometries(); i++) {
					distribution.add((geom.getGeometryN(i)).getArea());
				}
				int index = opRndChoice(distribution);
				return pointIn((geom.getGeometryN(index)));
			} 
			return pointIn((geom.getGeometryN(rand.nextInt(geom.getNumGeometries()))));
		}

		return null;

	
		
	}

	private Integer opRndChoice(final List<Double> distribution) {
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

	@Override
	public List<Point> pointIn(Geometry geom, int nb) {
		List<Point> points = new ArrayList<>();
		for (int i = 0; i < nb; i++)
			points.add(pointIn(geom));
		return points;
	}
	
	public Random getRand() {
		return rand;
	}

	public void setRand(Random rand) {
		this.rand = rand;
	}

}
