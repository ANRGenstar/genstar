package core.metamodel.value;

import core.metamodel.IAttribute;
import core.util.data.GSEnumDataType;

/**
 * Define a set of value both concretely and theoretically as it characterize a given attribute.
 * Theoretical space is define using the {@link #getValue(String)} method while concrete space
 * is define using {@link #retrieveValue(String)}. First one is a prior requirement to retrieve
 * value from concrete space
 * 
 * @author kevinchapuis
 *
 * @param <T>
 */
public interface IValueSpace<T extends IValue> {
	
	/**
	 * Get the value from the theoretical value space if complied with. If the
	 * proposed value does not feet theoretical requirement (e.g. characters chain
	 * within a continuous "double" value space) then an exception is raised
	 * 
	 * @param value
	 * @return
	 */
	public T getValue(String value) throws IllegalArgumentException;
	
	/**
	 * Retrieve the value from the operational value space as it as been defined. If
	 * the value have not been define in the theoretical space first using {@link #getValue(String)}
	 * then an exception is raised
	 * 
	 * @param value
	 * @return
	 * @throws NullPointerException
	 */
	public T retrieveValue(String value) throws NullPointerException;
	
	/**
	 * The tagged value type
	 * 
	 * @return
	 */
	public GSEnumDataType getType();
	
	/**
	 * The attribute this value space defines
	 * 
	 * @return
	 */
	public IAttribute getAttribute();
	
}
