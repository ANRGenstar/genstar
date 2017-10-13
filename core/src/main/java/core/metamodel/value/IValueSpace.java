package core.metamodel.value;

import java.util.Set;

import core.metamodel.IAttribute;
import core.util.data.GSEnumDataType;

/**
 * Define a set of value both concretely and theoretically as it characterize a given attribute.
 * Theoretical space is define using the {@link #addValue(String)} method while concrete space
 * is define using {@link #getValue(String)}. First one is a prior requirement to retrieve
 * value from concrete space
 * 
 * @author kevinchapuis
 *
 * @param <V>
 */
public interface IValueSpace<V extends IValue> extends Set<V> {
	
	/**
	 * Get the value from the theoretical value space if complied with. If the
	 * proposed value does not feet theoretical requirement (e.g. characters chain
	 * within a continuous "double" value space) then an exception is raised
	 * 
	 * @param value
	 * @return
	 */
	public V addValue(String value) throws IllegalArgumentException;
	
	/**
	 * Retrieve the value from the operational value space as it as been defined. If
	 * the value have not been define in the theoretical space first using {@link #addValue(String)}
	 * then an exception is raised
	 * 
	 * @param value
	 * @return
	 * @throws NullPointerException
	 */
	public V getValue(String value) throws NullPointerException;
	
	/**
	 * States if passed value is a theoretical valid candidate to be part
	 * of this value space
	 * @param value
	 * @return
	 */
	public boolean isValidCandidate(String value);
	
	/**
	 * 
	 */
	public GSEnumDataType getType();
	
	/**
	 * Return the *empty value* for this value space
	 * 
	 */
	public V getEmptyValue();
	
	/**
	 * Force the *empty value* to be made of {@link IValue} made from {@code value} parameter
	 * 
	 * @param value
	 */
	void setEmptyValue(String value);
	
	/**
	 * The attribute this value space defines
	 * 
	 * @return
	 */
	public IAttribute<V> getAttribute();
		
}
