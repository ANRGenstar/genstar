package core.metamodel.attribute;

import core.metamodel.value.IValue;

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
	 * The theoretical space of value that characterize this attribute
	 * 
	 * @return
	 */
	public IValueSpace<V> getValueSpace();
	
}
