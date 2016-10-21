package io.data.geo.attribute;

import java.util.Collection;

import org.opengis.feature.Feature;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;


/**
 * TODO: javadoc
 * 
 * TODO: move to general IAttribute contract
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
	 * Return value is of type {@link IGeoValue} that can encapsulate either numerical or nominal value
	 * 
	 * @param property
	 * @return {@link IGeoValue}
	 * @throws IllegalAttributeValueRequest 
	 */
	public IGeoValue getValue(String property);
	
	/**
	 * The collection of values associated with this attribute
	 * 
	 * @return
	 */
	public Collection<IGeoValue> getValues();
	
	/**
	 * Says if the property's attribute represents a null (nill or noData) value.
	 * 
	 * @return a primitive boolean
	 */
	public boolean isNoDataValue(String property);
	
	/**
	 * The name of this attribute
	 * 
	 * @return {@link String} of the name
	 */
	public String getGenstarName();
	
	/**
	 * The point characterizes the attribute position
	 * 
	 * @return {@link Point}
	 */
	public Point getPosition();
	
	/**
	 * The geometry charcaterizes the attribute
	 * 
	 * @return {@link Geometry}
	 */
	public Geometry getGeometry();
	
	/**
	 * The area this geometry's attribute cover 
	 * 
	 * @return double value
	 */
	public double getArea();
	
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
