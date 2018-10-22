package core.metamodel.attribute.emergent;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import core.metamodel.attribute.IAttribute;
import core.metamodel.attribute.emergent.aggregator.IAggregatorValueFunction;
import core.metamodel.attribute.emergent.filter.IEntityChildFilter;
import core.metamodel.attribute.emergent.transposer.ITransposeValueFunction;
import core.metamodel.entity.IEntity;
import core.metamodel.value.IValue;

/**
 * Aggregate several values of sub-entity into one "summary" value. Default aggregation could be sum for number,
 * concatenation for nominal value, sum of indexes for ordered value, etc.
 * 
 * @see ITransposeValueFunction
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
			U extends IAttribute<V>, V extends IValue> 
		extends AEntityEmergentFunction<E, U, V> {

	public static final String SELF = "AGG ATTRIBUTE FUNCTION"; 
	public static final String AGGREGATOR = "AGGREGATOR";
	
	private IAggregatorValueFunction<V> aggregator;
	
	public EntityAggregatedAttributeFunction(IAttribute<V> referent, 
			IAggregatorValueFunction<V> aggregator, IEntityChildFilter filter, IValue... matches) {
		super(referent, filter, matches);
		this.aggregator = aggregator;
	}

	@Override
	public V apply(E entity, U attribute) {
		return aggregator.transpose(this.getFilter().retain(entity.getChildren(), this.getMatchers())
				.stream().map(e -> attribute.getValueSpace()
						.getValue(entity.getValueForAttribute(attribute.getAttributeName()).getStringValue()))
				.collect(Collectors.toSet()), this.getReferentAttribute().getValueSpace());
	}
	
	@Override
	public Collection<Set<IValue>> reverse(V value, U attribute) {
		// TODO not sure at all
		return Collections.singleton(new HashSet<>(aggregator.reverse(value, attribute.getValueSpace())));
	}
	
	@JsonProperty(AGGREGATOR)
	public IAggregatorValueFunction<V>  getAggregationFunction(){
		return aggregator;
	}
	
	@JsonProperty(AGGREGATOR)
	public void setAggregatorFunction(IAggregatorValueFunction<V> function) {
		this.aggregator = function;
	}

}
