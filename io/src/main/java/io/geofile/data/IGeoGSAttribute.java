package io.geofile.data;

import java.util.Collection;

import org.opengis.feature.Feature;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;

/**
 * TODO: javadoc
 * 
 * @author kevinchapuis
 *
 */
public interface IGeoGSAttribute {

	/**
	 * A collection of property name for this geographical attribute
	 * 
	 * @return
	 */
	public Collection<String> getPropertiesAttribute();
	
	/**
	 * The value associated with this attribute. 
	 * Return value is of type {@link IGeoData} that can encapsulate either numerical or nominal value
	 * 
	 * @param property
	 * @return {@link IGeoData}
	 * @throws IllegalAttributeValueRequest 
	 */
	public IGeoData getValue(String property);
	
	/**
	 * Says if the property's attribute represents a null (nill or noData) value.
	 * 
	 * @return
	 */
	public boolean isNoDataValue(String property);
	
	/**
	 * The name of this attribute
	 * 
	 * @return
	 */
	public String getGenstarName();
	
	/**
	 * The geometry characterizes the attribute
	 * 
	 * @return
	 */
	public Geometry getPosition();
	
	/**
	 * A specific case of cast: this attribute could be return in form of extended GeoTools {@link Feature}, that is a {@link GSFeature}
	 * 
	 * @return
	 */
	public GSFeature transposeToGenstarFeature();
	
	/**
	 * Same as {@link #transposeToGenstarFeature()} but with a specified coordinate reference system
	 * 
	 * @param crs
	 * @return
	 */
	public GSFeature transposeToGenstarFeature(CoordinateReferenceSystem crs);
	
}
