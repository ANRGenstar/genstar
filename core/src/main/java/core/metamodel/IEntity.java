package core.metamodel;

import java.util.Collection;

import com.vividsolutions.jts.geom.Point;

import core.io.geo.entity.AGeoEntity;

/**
 * An entity might represent an household, an individual, or even a geographical entity etc.
 * 
 * @author gospl-team
 *
 */
public interface IEntity<A extends IAttribute<V>, V extends IValue> {

	/**
	 * returns the list of the attributes for which the entity might have values
	 * @return
	 */
	public Collection<A> getAttributes();
	
	/**
	 * returns the value for an attribute if any; the value might be null if no
	 * value is defined; raises an exception if the attribute is not declared for this entity
	 * @param attribute
	 * @return
	 */
	public V getValueForAttribute(A attribute);

	/**
	 * returns values for each attributes of the entity
	 * 
	 * @return
	 */
	public Collection<V> getValues();
	 
	// SPLL CONNECTOR
	
	public Point getLocation();
	
	public AGeoEntity getNest();
	
	public void setLocation(Point location);
	
	public void setNest(AGeoEntity entity);
	
	public V getValueForAttribute(String property);
	
	
	
}
