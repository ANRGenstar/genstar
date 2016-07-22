package io.github.gospl.metamodel;

import java.util.Collection;

/**
 * An entity might represent an household, an individual, etc.
 * 
 * @author gospl-team
 *
 */
public interface IEntity {

	/**
	 * returns the list of the attributes for which the entity might have values
	 * @return
	 */
	public Collection<IAttribute> getAttributes();
	
	/**
	 * returns the value for an attribute if any; the value might be null if no
	 * value is defined; raises an exception if the attribute is not declared for this entity
	 * @param a
	 * @return
	 */
	public Object getValueForAttribute(IAttribute a) throws UndefinedAttributeException;
	
	
	
}
