package io.data.geo.attribute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.jts.GeometryBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import io.data.geo.GeotiffFile;

public class GSPixel implements IGeoGSAttribute {
	
	private Envelope2D pixel;
	private Number[] bandsData;

	private CoordinateReferenceSystem crs;
	private List<Number> noData = new ArrayList<>();
	
	private GSPixel(Envelope2D pixel, Number[] bandsData, 
			CoordinateReferenceSystem crs, List<Number> noData) {
		this.pixel = pixel;
		this.bandsData = bandsData;
		this.crs = crs;
		this.noData.addAll(noData);
	}
	
	public GSPixel(Envelope2D pixel, Number[] bandsData, CoordinateReferenceSystem crs, double[] noDataValues){
		this(pixel, bandsData, crs, Arrays.stream(noDataValues).boxed().collect(Collectors.toList()));
	}
	
	public GSPixel(Envelope2D pixel, Number[] bandsData, CoordinateReferenceSystem crs, Number noData){
		this(pixel, bandsData, crs, Arrays.asList(noData));
	}
	
	public GSPixel(Envelope2D pixel, Number[] bandsData, CoordinateReferenceSystem crs) {
		this(pixel, bandsData, crs, GeotiffFile.DEF_NODATA);
	}
	
	public GSPixel(Envelope2D pixel, Number[] bandsData) {
		this(pixel, bandsData, DefaultGeographicCRS.WGS84, GeotiffFile.DEF_NODATA);
	}
	
	@Override
	public double getArea() {
		return pixel.getWidth() * pixel.getHeight();
	}

	@Override
	public Point getPosition() {
		return new GeometryBuilder().point(pixel.getCenterX(), pixel.getCenterY());
		//return JTSFactoryFinder.getGeometryFactory().createPoint(new Coordinate(pixel.getCenterX(), pixel.getCenterY()));
	}
	
	@Override
	public Geometry getGeometry() {
		return new GeometryBuilder().polygon(
				pixel.getMinX(), pixel.getMinY(),
				pixel.getMinX(), pixel.getMaxY(),
				pixel.getMaxX(), pixel.getMinY(),
				pixel.getMaxX(), pixel.getMaxY());
	}
	
	public double getMaxX() {
		return pixel.getMaxX();
	}
	
	public double getMaxY() {
		return pixel.getMaxY();
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
		b.add("Geometry", Polygon.class); // then add geometry
		SimpleFeatureType TYPE = b.buildFeatureType();
		
		SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);
		featureBuilder.add(getGeometry());
		SimpleFeature feature = featureBuilder.buildFeature(null);

		return new GSFeature(feature);
	}
	
	@Override
	public String getGenstarName(){
		return "px ["+pixel.getCenterX()+";"+pixel.getCenterY()+"]";
	}
	
	@Override
	public Collection<String> getPropertiesAttribute(){
		return IntStream.range(0, bandsData.length).boxed().map(i -> i.toString()).collect(Collectors.toList());
	}

	@Override
	public IGeoValue getValue(String attribute) {
		if(!attribute.matches("\\d+"))
			return new GSGeoData(noData.get(0));
		return new GSGeoData(bandsData[Integer.valueOf(attribute)]);
	}
	
	@Override
	public Collection<IGeoValue> getValues() {
		Collection<IGeoValue> data = new ArrayList<>();
		for(int i = 0; i < bandsData.length; i++)
			data.add(getValue(String.valueOf(i)));
		return data;
	}
	
	@Override
	public boolean isNoDataValue(String attribute) {
		return noData.stream().anyMatch(num -> bandsData[Integer.valueOf(attribute)].toString().equals(num.toString()));
	}
	
	@Override
	public String toString() {
		return this.getGenstarName();
	}
	
}
