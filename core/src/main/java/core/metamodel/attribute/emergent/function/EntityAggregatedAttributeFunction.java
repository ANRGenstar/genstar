package core.metamodel.attribute.emergent.function;

import java.util.stream.Collectors;

import core.metamodel.attribute.IAttribute;
import core.metamodel.attribute.emergent.filter.IEntityChildFilter;
import core.metamodel.attribute.emergent.function.aggregator.IAggregateValueFunction;
import core.metamodel.entity.IEntity;
import core.metamodel.value.IValue;

public class EntityAggregatedAttributeFunction<E extends IEntity<? extends IAttribute<? extends IValue>>,
			A extends IAttribute<IV>, IV extends IValue, RV extends IValue> 
		extends AEntityEmergentFunction<E, A, RV>
		implements IEntityEmergentFunction<E, A, RV> {

	private IAggregateValueFunction<RV, IV> aggregator;

	public EntityAggregatedAttributeFunction(
			IAggregateValueFunction<RV, IV> aggregator,
			IEntityChildFilter filter,
			IValue... matches) {
		super(filter, matches);
		this.aggregator = aggregator;
	}

	@Override
	public RV apply(E entity, A attribute) {
		return aggregator.aggregate(this.getFilter().retain(entity.getChildren(), this.getMatchers())
				.stream().map(e -> attribute.getValueSpace()
						.getValue(entity.getValueForAttribute(attribute.getAttributeName()).getStringValue()))
				.collect(Collectors.toSet()));
	}

}
