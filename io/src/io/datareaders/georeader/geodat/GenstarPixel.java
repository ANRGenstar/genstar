package io.datareaders.georeader.geodat;

import java.util.Arrays;
import java.util.List;

import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class GenstarPixel implements IGeoGenstarAttributes<Number, Double> {

	int x, y;
	
	Number[] bandsData;
	
	public GenstarPixel(int x, int y, Number[] bandsData) {
		this.x = x;
		this.y = y;
		this.bandsData = bandsData;
	}
	
	/**
	 * Coordinate in [x;y] form
	 * 
	 * @return int[]
	 */
	public int[] getCoordinate(){
		return new int[]{x, y};
	}
	
	@Override
	public GenstarFeature transposeToGenstarFeature() {
		SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();
		b.setName(this.getGenstarName());		
		b.setCRS(DefaultGeographicCRS.WGS84); // set crs first
		b.add("location", Point.class); // then add geometry
		SimpleFeatureType TYPE = b.buildFeatureType();
		
		SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);
		GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
		Point point = geometryFactory.createPoint(new Coordinate(this.x, this.y));
		featureBuilder.add(point);
		SimpleFeature feature = featureBuilder.buildFeature(null);

		return new GenstarFeature(feature);
	}
	
	@Override
	public String getGenstarName(){
		return "px ["+x+";"+y+"]";
	}
	
	@Override
	public List<Number> getData(){
		return Arrays.asList(bandsData);
	}

	@Override
	public Double getValue(Number attribute) {
		return attribute.doubleValue();
	}
	
}
