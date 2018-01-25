package core.metamodel.attribute.emergent.function;

import core.metamodel.attribute.IAttribute;
import core.metamodel.attribute.IValueSpace;
import core.metamodel.attribute.emergent.filter.IEntityChildFilter;
import core.metamodel.entity.IEntity;
import core.metamodel.value.IValue;

/**
 * Function that will return the value for attribute of a particular sub entities. In this case, filter and matches
 * are mandatory, because they make it possible to elicit only one individual. As define by filters, if several sub-entities
 * correspond to the filter restriction, there is an inner ordering process that will sort sub entities, hence allowing to
 * chose the first one.
 * 
 * @author kevinchapuis
 *
 * @param <E>
 * @param <A>
 * @param <V>
 */
public class EntityValueForAttributeFunction<E extends IEntity<? extends IAttribute<? extends IValue>>,
		 A extends IAttribute<V>, V extends IValue> 
	extends AEntityEmergentFunction<E, A, V>
	implements IEntityEmergentFunction<E, A, V> {

	public EntityValueForAttributeFunction(IValueSpace<V> vs, IEntityChildFilter filter, IValue... matches) {
		super(vs, filter, matches);
		if(filter == null || matches == null)
			throw new IllegalArgumentException("Value for attribute function cannot be instantiated "
					+ "without filter and matches");
	}

	@Override
	public V apply(E entity, A attribute) {
		return attribute.getValueSpace().getValue(
				super.getFilter().choseOne(entity.getChildren(), super.getMatchers())
				.getValueForAttribute(attribute.getAttributeName())
				.getStringValue());
	}

}
