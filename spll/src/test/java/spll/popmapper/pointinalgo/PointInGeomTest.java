package spll.popmapper.pointinalgo;

import java.util.Arrays;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import core.util.random.GenstarRandom;
import spll.localizer.pointInalgo.CentroidPointinLocalizer;
import spll.localizer.pointInalgo.RandomPointInLocalizer;

public class PointInGeomTest {

	static Geometry point;
	static Geometry line;
	static Geometry polygon;
	static Geometry multipoint;
	static Geometry multiline;
	static Geometry multipolygon;
	static Geometry geometryCollection;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		setup();
	}

	
	@Test
	public void testRandomPointInLocalizerInPoint() {
		RandomPointInLocalizer rpI = new RandomPointInLocalizer(GenstarRandom.getInstance());
		Point pt = rpI.pointIn(point);
		assert pt.getCoordinate().equals(point.getCoordinate());
	}
	
	@Test
	public void testRandomPointInLocalizerInLine() {
		RandomPointInLocalizer rpI = new RandomPointInLocalizer(GenstarRandom.getInstance());
		Point pt = rpI.pointIn(line);
		assert pt.distance(pt) < 0.1;
	}
	
	@Test
	public void testRandomPointInLocalizerInPolygon() {
		RandomPointInLocalizer rpI = new RandomPointInLocalizer(GenstarRandom.getInstance());
		Point pt = rpI.pointIn(polygon);
		assert pt.intersects(polygon);
	}
	
	@Test
	public void testRandomPointInLocalizerInMultiPoint() {
		RandomPointInLocalizer rpI = new RandomPointInLocalizer(GenstarRandom.getInstance());
		Point pt = rpI.pointIn(multipoint);
		assert multipoint.intersects(pt);
	}

	@Test
	public void testRandomPointInLocalizerInMultiLine() {
		RandomPointInLocalizer rpI = new RandomPointInLocalizer(GenstarRandom.getInstance());
		Point pt = rpI.pointIn(multiline);
		assert pt.distance(pt) < 0.1;
	}
	
	@Test
	public void testRandomPointInLocalizerInMultiPolygon() {
		RandomPointInLocalizer rpI = new RandomPointInLocalizer(GenstarRandom.getInstance());
		Point pt = rpI.pointIn(multipolygon);
		assert pt.intersects(multipolygon);
	}
	
	@Test
	public void testRandomPointInLocalizerInGeomCollection() {
		RandomPointInLocalizer rpI = new RandomPointInLocalizer(GenstarRandom.getInstance());
		Point pt = rpI.pointIn(geometryCollection);
		assert pt.distance(pt) < 0.1;
	}
	
	@Test
	public void testCentroidPointinLocalizerInPoint() {
		CentroidPointinLocalizer rpI = new CentroidPointinLocalizer();
		Point pt = rpI.pointIn(point);
		assert pt.getCoordinate().equals(point.getCoordinate());
	}
	
	@Test
	public void testCentroidPointinLocalizerInLine() {
		CentroidPointinLocalizer rpI = new CentroidPointinLocalizer();
		Point pt = rpI.pointIn(line);
		assert pt.distance(pt) < 0.1;
	}
	
	@Test
	public void testCentroidPointinLocalizerInPolygon() {
		CentroidPointinLocalizer rpI = new CentroidPointinLocalizer();
		Point pt = rpI.pointIn(polygon);
		assert pt.intersects(polygon);
	}
	
	@Test
	public void testCentroidPointinLocalizerInMultiPoint() {
		CentroidPointinLocalizer rpI = new CentroidPointinLocalizer();
		Point pt = rpI.pointIn(multipoint);
		assert multipoint.intersects(pt);
	}

	@Test
	public void testCentroidPointinLocalizerInMultiLine() {
		CentroidPointinLocalizer rpI = new CentroidPointinLocalizer();
		Point pt = rpI.pointIn(multiline);
		assert pt.distance(pt) < 0.1;
	}
	
	@Test
	public void testCentroidPointinLocalizerInMultiPolygon() {
		CentroidPointinLocalizer rpI = new CentroidPointinLocalizer();
		Point pt = rpI.pointIn(multipolygon);
		assert pt.intersects(multipolygon);
	}
	
	@Test
	public void testCentroidPointinLocalizerInGeomCollection() {
		CentroidPointinLocalizer rpI = new CentroidPointinLocalizer();
		Point pt = rpI.pointIn(geometryCollection);
		assert pt.distance(pt) < 0.1;
	}
	
	private static void setup(){
		GeometryFactory factory = new GeometryFactory();
		Coordinate c1 = new Coordinate(0.0, 0.0);
		Coordinate c2 = new Coordinate(10.0, 10.0);
		Coordinate c3 = new Coordinate(4.0, 20.0);
		Coordinate c4 = new Coordinate(30.0, 20.0);
		Coordinate c5 = new Coordinate(4.0, 10.0);
		
		point = factory.createPoint(c1);
		Coordinate[] cs = new Coordinate[3];
		cs[0] = c1; cs[1] = c2;cs[2] = c3;
		line = factory.createLineString(cs);
		Coordinate[] cs2 = new Coordinate[2];
		cs2[0] = c4; cs2[1] = c5;
		Geometry line2 = factory.createLineString(cs2);
		polygon = point.buffer(10.0);
		multipoint = factory.createMultiPoint(cs);  
		LineString[] lines = new LineString[2]; 
		lines[0] = (LineString) line;lines[1] = (LineString) line2;
		multiline = factory.createMultiLineString(lines);
		Polygon[] polygons = new Polygon[2]; 
		polygons[0] = (Polygon) polygon;polygons[1] = (Polygon) factory.createPoint(c2).buffer(30.0);
		multipolygon = factory.createMultiPolygon(polygons);
		List<Geometry> geomList = Arrays.asList(point, line, polygon);
		geometryCollection = factory.buildGeometry(geomList);
	}
	
}
