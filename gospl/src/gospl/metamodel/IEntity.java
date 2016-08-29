package gospl.metamodel;

import java.util.Collection;

import gospl.metamodel.attribut.IAttribute;
import gospl.metamodel.attribut.value.IValue;
import gospl.metamodel.exception.UndefinedAttributeException;

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
	public IValue getValueForAttribute(IAttribute a) throws UndefinedAttributeException;
	
	
	
}
