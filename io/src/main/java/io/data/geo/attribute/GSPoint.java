package io.data.geo.attribute;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class GSPoint implements IGeoGSAttribute {

	private IGeoValue[] data;
	private final double x,y;
	
	private CoordinateReferenceSystem crs;
	
	public GSPoint(double x, double y, IGeoValue[] data, CoordinateReferenceSystem crs) {
		this.x = x;
		this.y = y;
		this.data = data;
		this.crs = crs;
	}
	
	@Override
	public Point getPosition() {
		return new GeometryFactory().createPoint(new Coordinate(x, y));
	}

	@Override
	public Geometry getGeometry() {
		return getPosition();
	}

	@Override
	public double getArea() {
		return 0;
	}
	
	@Override
	public Collection<String> getPropertiesAttribute() {
		return IntStream.range(0, data.length).boxed().map(i -> i.toString()).collect(Collectors.toList());
	}

	@Override
	public IGeoValue getValue(String property) {
		if(!property.matches("\\d+"))
			return new GSGeoData(null);
		return new GSGeoData(data[Integer.valueOf(property)]);
	}
	
	@Override
	public Collection<IGeoValue> getValues() {
		return Arrays.asList(data);
	}

	@Override
	public boolean isNoDataValue(String property) {
		if(this.getValue(property).getValue() == null)
			return true;
		return false;
	}

	@Override
	public String getGenstarName() {
		return "point ["+x+";"+y+"]";
	}

	@Override
	public GSFeature transposeToGenstarFeature() {
		return this.transposeToGenstarFeature(this.crs);
	}

	@Override
	public GSFeature transposeToGenstarFeature(CoordinateReferenceSystem crs) {
		SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();
		b.setName(this.getGenstarName());		
		b.setCRS(crs); // set crs first
		b.add("location", Point.class); // then add geometry
		SimpleFeatureType TYPE = b.buildFeatureType();
		
		SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);
		featureBuilder.add(getGeometry());
		SimpleFeature feature = featureBuilder.buildFeature(null);

		return new GSFeature(feature);
	}

}
