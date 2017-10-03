package core.metamodel;

import core.metamodel.value.IValue;
import core.metamodel.value.IValueSpace;

/**
 * Attribute (of for instance an individual or household)
 * 
 * @author gospl-team
 *
 */
public interface IAttribute {

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
	public IValueSpace<IValue> getAttributeValueSpace();
	
}
