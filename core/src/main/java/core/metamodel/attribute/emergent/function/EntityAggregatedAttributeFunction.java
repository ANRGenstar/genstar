package core.metamodel.attribute.emergent.function;

import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import core.metamodel.attribute.IAttribute;
import core.metamodel.attribute.emergent.filter.IEntityChildFilter;
import core.metamodel.attribute.emergent.function.aggregator.IAggregateValueFunction;
import core.metamodel.entity.IEntity;
import core.metamodel.value.IValue;

/**
 * Aggregate several values of sub-entity into one "summary" value. Default aggregation could be sum for number,
 * concatenation for nominal value, sum of indexes for ordered value, etc.
 * 
 * @see IAggregateValueFunction
 * 
 * @author kevinchapuis
 *
 * @param <E>
 * @param <U>
 * @param <IV>
 * @param <RV>
 */
@JsonTypeName(EntityAggregatedAttributeFunction.SELF)
public class EntityAggregatedAttributeFunction<E extends IEntity<? extends IAttribute<? extends IValue>>,
			U extends IAttribute<IV>, IV extends IValue, RV extends IValue> 
		extends AEntityEmergentFunction<E, U, RV>
		implements IEntityEmergentFunction<E, U, RV> {

	public static final String SELF = "AGG ATTRIBUTE FUNCTION"; 
	public static final String AGGREGATOR = "AGGREGATOR";
	
	private IAggregateValueFunction<RV, IV> aggregator;

	public EntityAggregatedAttributeFunction(
			IAggregateValueFunction<RV, IV> aggregator,
			IEntityChildFilter filter,
			IValue... matches) {
		super(aggregator.getReferentAttribute(), filter, matches);
		this.aggregator = aggregator;
	}

	@Override
	public RV apply(E entity, U attribute) {
		return aggregator.aggregate(this.getFilter().retain(entity.getChildren(), this.getMatchers())
				.stream().map(e -> attribute.getValueSpace()
						.getValue(entity.getValueForAttribute(attribute.getAttributeName()).getStringValue()))
				.collect(Collectors.toSet()));
	}
	
	@JsonProperty(AGGREGATOR)
	public IAggregateValueFunction<RV, IV>  getAggregationFunction(){
		return aggregator;
	}
	
	@JsonProperty(AGGREGATOR)
	public void setAggregatorFunction(IAggregateValueFunction<RV, IV> function) {
		this.aggregator = function;
	}

}
