package core.metamodel.attribute.emergent.aggregator;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import core.metamodel.attribute.IValueSpace;
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
	        @JsonSubTypes.Type(value = IAggregatorValueFunction.class)
	    })
@JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class)
public interface ITransposeValueFunction<IV extends IValue, RV extends IValue> {

	public static final String ID = "TYPE ID";
	
	/**
	 * The main aggregation method. Return value type can be of another type than of input values
	 * @param values
	 * @return a unique aggregated value
	 */
	public RV transpose(Collection<IV> values, IValueSpace<RV> valueSpace);
	
}
