package core.metamodel;

import java.util.Set;


/**
 * Attribute (of for instance an individual or household)
 * 
 * @author gospl-team
 *
 */
public interface IAttribute<V extends IValue> {

	/**
	 * The name of the attribute
	 * 
	 * @return the name - {@link String}
	 */
	public String getAttributeName();
	
// ------------------------- value related methods ------------------------- //
	
	/**
	 * All values this attribute can take as part of an entity
	 * 
	 * @return
	 */
	public Set<V> getValues();


	/**
	 * All values this attribute can take as part of an entity
	 * 
	 * @return
	 */
	public Set<String> getValuesAsString();

	/**
	 * 
	 * If the value set for this {@link IAttribute} is empty then values could be set to the ones in parameter. 
	 * Otherwise, values could not be change. Note that values should have different textual representations.
	 * 
	 * @param values
	 * @return <code>true</code> if it actually set values, <code>false</code> if not 
	 */
	public boolean setValues(Set<V> values);

	/**
	 * Return the empty default {@link IValue} for this {@link IAttribute}
	 * 
	 * @return
	 */
	public V getEmptyValue();
	
	/**
	 * The empty default {@link IValue} for this {@link IAttribute}
	 * 
	 * @param emptyValue
	 */
	public void setEmptyValue(V emptyValue);
	
	/** 
	 * get the value having this name
	 * @param name
	 * @return
	 */
	public V getValue(String name);
	
	/**
	 * returns true if it contains a value having this string representation
	 * @param name
	 * @return
	 */
	public boolean hasValue(String name);
	
}
