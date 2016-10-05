package io.datareaders.georeader.geodat;

import java.util.Collection;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;


public interface IGeoGSAttribute {

	public Collection<String> getPropertiesAttribute();
	
	public IGeoData getValue(String property);
	
	public String getGenstarName();
	
	public Geometry getPosition();
	
	public GSFeature transposeToGenstarFeature();
	
	public GSFeature transposeToGenstarFeature(CoordinateReferenceSystem crs);
	
}
