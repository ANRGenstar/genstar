package io.datareaders.georeader.geodat;

import java.util.Collection;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;


public interface IGeoGSAttribute<A, D> {

	public Collection<A> getProperties();
	
	public D getValue(A attribute);
	
	public String getGenstarName();
	
	public Geometry getPosition();
	
	public GSFeature transposeToGenstarFeature();
	
	public GSFeature transposeToGenstarFeature(CoordinateReferenceSystem crs);
	
}
