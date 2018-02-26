package core.metamodel.attribute.emergent;

import com.fasterxml.jackson.annotation.JsonTypeName;

import core.metamodel.attribute.IAttribute;
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
 * @param <U>
 * @param <V>
 */
@JsonTypeName(EntityValueForAttributeFunction.SELF)
public class EntityValueForAttributeFunction<E extends IEntity<? extends IAttribute<? extends IValue>>,
		 U extends IAttribute<V>, V extends IValue> 
	extends AEntityEmergentFunction<E, U, V>
	implements IEntityEmergentFunction<E, U, V> {

	public static final String SELF = "VALUE FOR ATTRIBUTE FUNCTION";
	
	public EntityValueForAttributeFunction(IAttribute<V> referent, IEntityChildFilter filter, IValue... matches) {
		super(referent, filter, matches);
	}

	@Override
	public V apply(E entity, U attribute) {
		return attribute.getValueSpace().getValue(
				super.getFilter().choseOne(entity.getChildren(), super.getMatchers())
				.getValueForAttribute(attribute.getAttributeName())
				.getStringValue());
	}

}
