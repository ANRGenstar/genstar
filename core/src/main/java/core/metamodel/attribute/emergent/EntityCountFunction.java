package core.metamodel.attribute.emergent;

import com.fasterxml.jackson.annotation.JsonTypeName;

import core.metamodel.attribute.IAttribute;
import core.metamodel.attribute.emergent.filter.IEntityChildFilter;
import core.metamodel.entity.IEntity;
import core.metamodel.value.IValue;
import core.metamodel.value.numeric.IntegerValue;

/**
 * Count the number of sub entities. If no filter have been define, this function will just return the number of child.
 * When using any filter, it will count the number of entities having identified matches
 * 
 * @see IEntityChildFilter
 * 
 * @author kevinchapuis
 *
 * @param <E>
 * @param <U>
 */
@JsonTypeName(EntityCountFunction.SELF)
public class EntityCountFunction<E extends IEntity<? extends IAttribute<? extends IValue>>, U> 
	extends AEntityEmergentFunction<E, U, IntegerValue>
	implements IEntityEmergentFunction<E, U, IntegerValue> {
	
	public static final String SELF = "COUNT FUNCTION";
	
	public EntityCountFunction(IAttribute<IntegerValue> referent, IEntityChildFilter filter, IValue... matches) {
		super(referent, filter, matches);
	}

	@Override
	public IntegerValue apply(E entity, U useless) {
		return this.getReferentAttribute().getValueSpace().proposeValue(
				Integer.toString(super.getFilter() != null && super.getMatchers() != null ? 
					super.getFilter().retain(entity.getChildren(), super.getMatchers()).size() : 
						entity.getCountChildren())
				);
	}

}
