package core.metamodel.value;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import core.configuration.jackson.ValueSerializer;
import core.metamodel.attribute.IValueSpace;
import core.metamodel.value.binary.BooleanValue;
import core.metamodel.value.categoric.NominalValue;
import core.metamodel.value.categoric.OrderedValue;
import core.metamodel.value.numeric.ContinuousValue;
import core.metamodel.value.numeric.IntegerValue;
import core.metamodel.value.numeric.RangeValue;
import core.util.data.GSEnumDataType;

/**
 * The values that characterise Genstar's attribute of entity
 * 
 * @author kevinchapuis
 *
 */
@JsonTypeInfo(
		use = JsonTypeInfo.Id.NONE,
	    include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
    @JsonSubTypes.Type(value = BooleanValue.class),
    @JsonSubTypes.Type(value = NominalValue.class),
    @JsonSubTypes.Type(value = OrderedValue.class),
    @JsonSubTypes.Type(value = ContinuousValue.class),
    @JsonSubTypes.Type(value = IntegerValue.class),
    @JsonSubTypes.Type(value = RangeValue.class)
})
@JsonSerialize(using = ValueSerializer.class)
public interface IValue {
	
	public static final String VALUE = "INPUT VALUE";
	
	/**
	 * The type of data this value encapsulate
	 * 
	 * @return
	 */
	@JsonIgnore
	public GSEnumDataType getType();
	
	/**
	 * The value represented as a String
	 * 
	 * @return
	 */
	@JsonProperty(IValue.VALUE)
	public String getStringValue();
	
	/**
	 * The value space this value is part of
	 * 
	 * @return
	 */
	@JsonBackReference()
	public IValueSpace<? extends IValue> getValueSpace();
	
	/**
	 * Default hash code computation
	 * @return
	 */
	default int getHashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getStringValue() == null) ? 0 : getStringValue().hashCode());
		result = prime * result + ((getValueSpace() == null) ? 0 : getValueSpace().hashCode());
		return result;
	}

	/**
	 * Default equal implementation
	 * @param obj
	 * @return
	 */
	default boolean isEquals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IValue other = (IValue) obj;
		if (this.getStringValue() == null) {
			if (other.getStringValue() != null)
				return false;
		} else if (!this.getStringValue().equals(other.getStringValue()))
			return false;
		if (this.getValueSpace() == null) {
			if (other.getValueSpace() != null)
				return false;
		} else if (!this.getValueSpace().equals(other.getValueSpace()))
			return false;
		return true;
	}
	
}
