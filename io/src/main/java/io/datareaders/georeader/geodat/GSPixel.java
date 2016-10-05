package io.datareaders.georeader.geodat;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class GSPixel implements IGeoGSAttribute {
	
	double x, y;
	
	Number[] bandsData;

	private CoordinateReferenceSystem crs;
	
	public GSPixel(double x, double y, Number[] bandsData, CoordinateReferenceSystem crs) {
		this.x = x;
		this.y = y;
		this.bandsData = bandsData;
		this.crs = crs;
	}
	
	public GSPixel(int x, int y, Number[] bandsData) {
		this(x, y, bandsData, DefaultGeographicCRS.WGS84);
	}

	/**
	 * Coordinate in [x;y] form
	 * 
	 * @return int[]
	 */
	public double[] getCoordinate(){
		return new double[]{x, y};
	}

	@Override
	public Geometry getPosition() {
		//return new GeometryBuilder(crs).getPrimitiveFactory().createPoint(new double[]{x,y});
		return new GeometryFactory().createPoint(new Coordinate(x, y));
	}
	
	@Override
	public GSFeature transposeToGenstarFeature() {
		return transposeToGenstarFeature(this.crs);
	}
	
	@Override
	public GSFeature transposeToGenstarFeature(CoordinateReferenceSystem crs) {
		SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();
		b.setName(this.getGenstarName());		
		b.setCRS(crs); // set crs first
		b.add("location", Point.class); // then add geometry
		SimpleFeatureType TYPE = b.buildFeatureType();
		
		SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);
		GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
		Point point = geometryFactory.createPoint(new Coordinate(this.x, this.y));
		featureBuilder.add(point);
		SimpleFeature feature = featureBuilder.buildFeature(null);

		return new GSFeature(feature);
	}
	
	@Override
	public String getGenstarName(){
		return "px ["+x+";"+y+"]";
	}
	
	@Override
	public Collection<String> getPropertiesAttribute(){
		return IntStream.range(0, bandsData.length).boxed().map(i -> i.toString()).collect(Collectors.toList());
	}

	@Override
	public IGeoData getValue(String attribute) {
		return new GSGeoData(bandsData[Integer.valueOf(attribute)]);
	}
	
}
