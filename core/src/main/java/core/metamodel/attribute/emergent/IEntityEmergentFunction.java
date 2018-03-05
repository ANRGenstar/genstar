package core.metamodel.attribute.emergent;

import java.util.function.BiFunction;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import core.configuration.jackson.EmergentFunctionSerializer;
import core.metamodel.attribute.IAttribute;
import core.metamodel.attribute.emergent.filter.IEntityChildFilter;
import core.metamodel.entity.IEntity;
import core.metamodel.value.IValue;
import core.metamodel.value.IValueSpace;

/**
 * The main function to compute an emergent attribute value based on sub entity properties. The function contains a filter
 * that make possible to match entities before computing emergent attribute value. Hence, one can filter sub-entities and
 * then apply the function.
 * </p>
 * We identify 3 basic forms of emergent function:
 * <p><ul>
 * <li> {@link EntityCountFunction} : count the number of entities
 * <li> {@link EntityValueForAttributeFunction} : get the value for an attribute and one sub-entity (filter is here essential)
 * <li> {@link EntityAggregatedAttributeFunction} : get one value from several
 * </ul><p>
 * 
 * @author kevinchapuis
 *
 * @param <E>
 * @param <U>
 * @param <V>
 */
@JsonTypeInfo(
	      use = JsonTypeInfo.Id.NAME,
	      include = JsonTypeInfo.As.PROPERTY
	      )
@JsonSubTypes({
	        @JsonSubTypes.Type(value = EntityAggregatedAttributeFunction.class),
	        @JsonSubTypes.Type(value = EntityCountFunction.class),
	        @JsonSubTypes.Type(value = EntityValueForAttributeFunction.class)
	    })
@JsonSerialize(using = EmergentFunctionSerializer.class)
public interface IEntityEmergentFunction<
		E extends IEntity<? extends IAttribute<? extends IValue>>, U, V extends IValue> 
	extends BiFunction<E, U, V> {
	
	public static final String FILTER = "FILTER";
	public static final String MATCHERS = "MATCHERS";
	public static final String TYPE = "CLASS TYPE";
	
	/**
	 * Returns the filter that will select the appropriate child to compute emergent attribute value
	 * @return
	 */
	@JsonProperty(FILTER)
	public IEntityChildFilter getFilter();
	
	/**
	 * Defines the filter to be use to select sub entities
	 * @param filter
	 */
	@JsonProperty(FILTER)
	public void setFilter(IEntityChildFilter filter);
	
	/**
	 * Returns the values to be used to filter sub-entities
	 * @return
	 */
	@JsonProperty(MATCHERS)
	public IValue[] getMatchers();
	
	/**
	 * Defines the values to be used to filter sub-entities
	 * @param matchers
	 */
	@JsonProperty(MATCHERS)
	public void setMatchers(IValue... matchers);
	
	/**
	 * returns the value space attached to this function
	 * @return
	 */
	@JsonProperty(IValueSpace.REF_ATT)
	public IAttribute<V> getReferentAttribute();
	
	/**
	 * Define the value space attached to this function
	 * @param vs
	 */
	@JsonProperty(IValueSpace.REF_ATT)
	public void setReferentAttribute(IAttribute<V> referentAttribute);
	
}
