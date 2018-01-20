package core.metamodel.attribute.emergent;

import core.metamodel.attribute.IAttribute;
import core.metamodel.attribute.IValueSpace;
import core.metamodel.attribute.demographic.DemographicAttribute;
import core.metamodel.attribute.emergent.function.IEntityEmergentFunction;
import core.metamodel.entity.IEntity;
import core.metamodel.value.IValue;

/**
 * Attribute that can retrieve value based on emergent properties. It is based on
 * the implementation of a {@link IEntityEmergentFunction}
 * 
 * @see IEntityEmergentFunction
 * 
 * @author kevinchapuis
 *
 * @param <V>
 * @param <E>
 * @param <U>
 */
public class EmergentAttribute<V extends IValue, 
	E extends IEntity<? extends IAttribute<? extends IValue>>,
			U> 
	extends DemographicAttribute<V> {

	private IValueSpace<V> vs;
	private IEntityEmergentFunction<E, U, V> function;

	protected EmergentAttribute(String name) {
		super(name);
	}
	
	/**
	 * 
	 * WARNING: The given value space must be implicitly consistent with emergent value return by 
	 * the emergent function
	 * 
	 * @param name
	 * @param vs
	 * @param function
	 */
	public EmergentAttribute(String name, IValueSpace<V> vs, 
			IEntityEmergentFunction<E, U, V> function) {
		this(name);
		this.vs = vs;
		this.function = function;
		this.function.setValueSpace(vs);
	}
	
	/**
	 * The main method that can retrieve the value of an attribute based on
	 * any child properties
	 * 
	 * @param entity
	 * @param predicate
	 * @return
	 */
	public V getEmergentValue(E entity, U predicate) {
		return function.apply(entity, predicate);
	}

	@Override
	public IValueSpace<V> getValueSpace() {
		return vs;
	}

	@Override
	public void setValueSpace(IValueSpace<V> valueSpace) {
		this.vs = valueSpace;
	}

}
