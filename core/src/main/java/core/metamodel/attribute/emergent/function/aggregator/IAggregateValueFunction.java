package core.metamodel.attribute.emergent.function.aggregator;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import core.metamodel.attribute.IAttribute;
import core.metamodel.value.IValue;

/**
 * Define a unique method that will aggregate a collection of value to return a unique value.
 * 
 * @author kevinchapuis
 *
 * @param <RV>
 * @param <IV>
 */
@JsonTypeInfo(
	      use = JsonTypeInfo.Id.NAME,
	      include = JsonTypeInfo.As.PROPERTY
	      )
@JsonSubTypes({
	        @JsonSubTypes.Type(value = BooleanAggValueFunction.class),
	        @JsonSubTypes.Type(value = ContinueSumValueFunction.class),
	        @JsonSubTypes.Type(value = IntegerSumValueFunction.class),
	        @JsonSubTypes.Type(value = NominalAggValueFunction.class),
	        @JsonSubTypes.Type(value = OrderedAggValueFunction.class),
	        @JsonSubTypes.Type(value = RangeAggValueFunction.class)
	    })
@JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class)
public interface IAggregateValueFunction<RV extends IValue, IV extends IValue> {

	public static final String TYPE = "TYPE";

	/**
	 * The main aggregation method. Return value type can be of another type than of input values
	 * @param values
	 * @return a unique aggregated value
	 */
	public RV aggregate(Collection<IV> values);

	public IAttribute<RV> getReferentAttribute();
	
}
