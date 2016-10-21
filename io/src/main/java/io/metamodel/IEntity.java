package io.metamodel;

import java.util.Collection;

import io.metamodel.attribut.IAttribute;
import io.metamodel.attribut.value.IValue;
import io.metamodel.exception.UndefinedAttributeException;

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
	 * @param attribute
	 * @return
	 */
	public IValue getValueForAttribute(IAttribute attribute) throws UndefinedAttributeException;

	/**
	 * returns values for each attributes of the entity
	 * 
	 * @return
	 */
	public Collection<IValue> getValues();
	
}
