package core.metamodel.attribute.emergent.function;

import java.util.function.BiFunction;

import core.metamodel.attribute.IAttribute;
import core.metamodel.attribute.IValueSpace;
import core.metamodel.attribute.emergent.filter.IEntityChildFilter;
import core.metamodel.entity.IEntity;
import core.metamodel.value.IValue;

/**
 * The main function to compute an emergent attribute value based on sub entity property 
 * 
 * @author kevinchapuis
 *
 * @param <E>
 * @param <U>
 * @param <V>
 */
public interface IEntityEmergentFunction<
		E extends IEntity<? extends IAttribute<? extends IValue>>, U, V extends IValue> 
	extends BiFunction<E, U, V> {
	
	/**
	 * Returns the filter that will select the appropriate child to compute emergent attribute value
	 * @return
	 */
	public IEntityChildFilter getFilter();
	
	/**
	 * Defines the filter to be use to select sub entities
	 * @param filter
	 */
	public void setFilter(IEntityChildFilter filter);
	
	/**
	 * Returns the values to be used to filter sub-entities
	 * @return
	 */
	public IValue[] getMatchers();
	
	/**
	 * Defines the values to be used to filter sub-entities
	 * @param matchers
	 */
	public void setMatchers(IValue... matchers);
	
	/**
	 * returns the value space attached to this function
	 * @return
	 */
	public IValueSpace<V> getValueSpace();
	
	/**
	 * Define the value space attached to this function
	 * @param vs
	 */
	public void setValueSpace(IValueSpace<V> vs);
	
}
