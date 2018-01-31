package core.metamodel.attribute.emergent.function.aggregator;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import core.metamodel.attribute.IValueSpace;
import core.metamodel.value.IValue;

@JsonTypeInfo(
	      use = JsonTypeInfo.Id.CLASS,
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

	public RV aggregate(Collection<IV> values);

	public IValueSpace<RV> getValueSpace();
	
}
