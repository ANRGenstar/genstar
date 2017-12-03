package core.metamodel.value.numeric;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

import core.metamodel.attribute.IAttribute;
import core.metamodel.attribute.IValueSpace;
import core.util.data.GSDataParser;
import core.util.data.GSEnumDataType;

/**
 * TODO: javadoc
 * 
 * @author kevinchapuis
 *
 */
public class IntegerSpace implements IValueSpace<IntegerValue> {

	private static GSDataParser gsdp = new GSDataParser();

	private IntegerValue emptyValue;
	private Set<IntegerValue> values;

	private int min, max;

	private IAttribute<IntegerValue> attribute;

	public IntegerSpace(IAttribute<IntegerValue> attribute){
		this(attribute, Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	public IntegerSpace(IAttribute<IntegerValue> attribute, Integer min, Integer max) {
		this.values = new HashSet<>();
		this.emptyValue = new IntegerValue(this);
		this.attribute = attribute;
		this.min = min;
		this.max = max;
	}

	// -------------------- SETTERS & GETTER CAPACITIES -------------------- //

	@Override
	public IntegerValue addValue(String value) {
		if(!gsdp.getValueType(value).isNumericValue())
			throw new IllegalArgumentException("The string value "+value+" does not feet a discrete "
					+ "integer value template");

		int currentVal = gsdp.parseNumbers(value).intValue();
		if(currentVal < min || currentVal > max)
			throw new IllegalArgumentException("Proposed value "+value+" is "
					+ (currentVal < min ? "below" : "beyond") + " given bound ("
					+ (currentVal < min ? min : max) + ")");

		IntegerValue iv = null;
		try {
			iv = getValue(value);
		} catch (NullPointerException e) {
			iv = new IntegerValue(this, currentVal);
			values.add(iv);
		}
		return iv;
	}

	@Override
	public IntegerValue getValue(String value) throws NullPointerException {
		Optional<IntegerValue> opValue = values.stream()
				.filter(val -> val.getActualValue() == gsdp.parseNumbers(value).intValue())
				.findAny();
		if(opValue.isPresent())
			return opValue.get();
		throw new NullPointerException("The string value "+value+" is not comprise "
				+ "in the value space "+this.toString());
	}

	@Override
	public Set<IntegerValue> getValues(){
		return Collections.unmodifiableSet(values);
	}

	@Override
	public IntegerValue getEmptyValue() {
		return emptyValue;
	}

	@Override
	public void setEmptyValue(String value) {
		try {
			this.emptyValue = getValue(value);
		} catch (NullPointerException npe) {
			try {
				this.emptyValue = new IntegerValue(this, gsdp.getDouble(value).intValue());
			} catch (Exception e) {
				// If value is not a parsable integer or null just leave the
				// default value as it is 
			}
		}
	}

	@Override
	public boolean isValidCandidate(String value){
		if(!gsdp.getValueType(value).isNumericValue() 
				|| gsdp.parseNumbers(value).intValue() < min 
				|| gsdp.parseNumbers(value).intValue() > max)
			return false;
		return true;
	}

	@Override
	public GSEnumDataType getType() {
		return GSEnumDataType.Integer;
	}

	@Override
	public IAttribute<IntegerValue> getAttribute() {
		return attribute;
	}

	// ----------------------------------------------------- //

	@JsonProperty("max")
	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}

	@JsonProperty("min")
	public int getMin()	{
		return min;
	}

	public void setMin(int min) {
		this.min = min;
	}

	// ----------------------------------------------- //

	@Override
	public int hashCode() {
		return this.getHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return isEqual(obj);
	}
	
	@Override
	public String toString() {
		return this.toPrettyString();
	}

}
