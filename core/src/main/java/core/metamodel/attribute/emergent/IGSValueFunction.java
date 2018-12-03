package core.metamodel.attribute.emergent;

import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import core.configuration.jackson.EmergentFunctionSerializer;
import core.metamodel.attribute.Attribute;
import core.metamodel.attribute.MappedAttribute;
import core.metamodel.value.IValue;

/**
 * Define a unique method that will aggregate a collection of value to return a unique value.
 * 
 * @author kevinchapuis
 *
 * @param <V> The value to be returned that will be any {@link IValue}
 * @param <U> The input value to be transposed to <V>
 */
@JsonTypeInfo(
	      use = JsonTypeInfo.Id.NAME,
	      include = JsonTypeInfo.As.PROPERTY
	      )
@JsonSubTypes({
	        @JsonSubTypes.Type(value = AggregateValueFunction.class),
	        @JsonSubTypes.Type(value = CountValueFunction.class),
	        @JsonSubTypes.Type(value = EntityValueFunction.class)
	    })
@JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class)
@JsonSerialize(using = EmergentFunctionSerializer.class)
public interface IGSValueFunction<U, V extends IValue> extends Function<U, V> {

	public static final String ID = "TYPE ID";
	public static final String MAPPING = "VALUE MAPPING"; 
	
	@JsonProperty(MappedAttribute.REF)
	public Attribute<V> getReferent();
	
	@JsonProperty(MappedAttribute.REF)
	public void setReferent(Attribute<V> referent);
	
}
