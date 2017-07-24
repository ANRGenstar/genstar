package core.metamodel;

import java.util.Collection;

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
	 * Returns true if this entity contains this attribute
	 * @param a
	 * @return
	 */
	public boolean hasAttribute(A a);

	/**
	 * returns values for each attributes of the entity
	 * 
	 * @return
	 */
	public Collection<V> getValues();
	
	/**
	 * returns the value for an attribute if any; the value might be null if no
	 * value is defined; raises an exception if the attribute is not declared for this entity
	 * @param attribute
	 * @return
	 */
	public V getValueForAttribute(A attribute);
	
	/**
	 * returns the value for an attribute if any, based on attribute name. The name of 
	 * attribute should be access using {@link IAttribute#getAttributeName()}
	 * <p>
	 * @see #getValueForAttribute(IAttribute)
	 * 
	 * @param property
	 * @return
	 */
	public V getValueForAttribute(String property);

}
