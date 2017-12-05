package core.metamodel.attribute;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;

import core.metamodel.value.IValue;
import core.metamodel.value.binary.BinarySpace;
import core.metamodel.value.categoric.NominalSpace;
import core.metamodel.value.categoric.OrderedSpace;
import core.metamodel.value.numeric.ContinuousSpace;
import core.metamodel.value.numeric.IntegerSpace;
import core.metamodel.value.numeric.RangeSpace;
import core.util.data.GSEnumDataType;

/**
 * Define a set of value both concretely and theoretically as it characterize a given attribute.
 * Theoretical space is define using the {@link #addValue(String)} method while concrete space
 * is define using {@link #getValue(String)}. First one is a prior requirement to retrieve
 * value from concrete space
 * 
 * @author kevinchapuis
 *
 * @param <V>
 */
@JsonSubTypes({
	        @JsonSubTypes.Type(value = BinarySpace.class),
	        @JsonSubTypes.Type(value = NominalSpace.class),
	        @JsonSubTypes.Type(value = OrderedSpace.class),
	        @JsonSubTypes.Type(value = ContinuousSpace.class),
	        @JsonSubTypes.Type(value = IntegerSpace.class),
	        @JsonSubTypes.Type(value = RangeSpace.class)
	    })
@JsonPropertyOrder({ IValueSpace.TYPE, IValueSpace.VALUES })
public interface IValueSpace<V extends IValue> {
	
	public static final String REF_ATT = "REFERENCE ATTRIBUTE";
	
	public static final String EMPTY = "EMPTY VALUE";
	public static final String TYPE = "TYPE";
	public static final String VALUES = "VALUES";
	
	
	public V getInstanceValue(String value);
	
	/**
	 * Create, add and return requested value according to value space specification. Must be compliant
	 * with concrete value type (or throw an exception) - contract should be define by {@link #getInstanceValue(String)}.
	 * 
	 * @param value
	 * @return
	 */
	public V addValue(String value) throws IllegalArgumentException;
	
	/**
	 * Retrieve the value from the operational value space as it as been defined. If
	 * the value have not been define in the theoretical space first using {@link #addValue(String)}
	 * then an exception is raised
	 * 
	 * @param value
	 * @return
	 * @throws NullPointerException
	 */
	public V getValue(String value) throws NullPointerException;
	
	/**
	 * Give the set of value in this value space
	 * 
	 * @return
	 */
	@JsonProperty(VALUES)
	public Set<V> getValues();
	
	/**
	 * Return the *empty value* for this value space
	 * 
	 */
	@JsonProperty(EMPTY)
	public V getEmptyValue();
	
	/**
	 * Force the *empty value* to be made of {@link IValue} made from {@code value} parameter
	 * 
	 * @param value
	 */
	void setEmptyValue(String value);
	
	/**
	 * Return the type of value this space contains
	 */
	@JsonProperty(IValueSpace.TYPE)
	public GSEnumDataType getType();
	
	/**
	 * States if passed value is a theoretical valid candidate to be part
	 * of this value space
	 * @param value
	 * @return
	 */
	public boolean isValidCandidate(String value);
	
	/**
	 * The attribute this value space defines
	 * 
	 * @return
	 */
	@JsonProperty(IValueSpace.REF_ATT)
	@JsonBackReference(value = IValueSpace.REF_ATT)
	public IAttribute<V> getAttribute();
	
	// -------------------------------------------------------- //
	
	default String toPrettyString() {
		return "["+this.getType().toString()+"]";
	}
	
	/**
	 * Utility method to compute hash code
	 * @return
	 */
	@JsonIgnore
	default int getHashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getAttribute() == null) ? 0 : getAttribute().getAttributeName().hashCode());
		result = prime * result + getType().hashCode();
		return result;
	}

	/**
	 * Utility method to estimate equality
	 * @param obj
	 * @return
	 */
	default boolean isEqual(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IValueSpace<? extends IValue> other = (IValueSpace<?>) obj;
		if (getAttribute() == null) {
			if (other.getAttribute() != null)
				return false;
		} else if (!getAttribute().getAttributeName().equals(other.getAttribute().getAttributeName()))
			return false;
		if (!getType().equals(other.getType()))
			return false;
		return true;
	}
	
}
