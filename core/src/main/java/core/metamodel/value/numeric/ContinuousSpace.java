package core.metamodel.value.numeric;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

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
public class ContinuousSpace implements IValueSpace<ContinuousValue> {

	private static GSDataParser gsdp = new GSDataParser();

	private ContinuousValue emptyValue;
	private TreeSet<ContinuousValue> values;

	private double min, max;

	private IAttribute<ContinuousValue> attribute;

	public ContinuousSpace(IAttribute<ContinuousValue> attribute) {
		this(attribute, 0d, Double.MAX_VALUE);
	}

	public ContinuousSpace(IAttribute<ContinuousValue> attribute,
			Double min, Double max) {
		this.attribute = attribute;
		this.min = min;
		this.max = max;
		this.emptyValue = new ContinuousValue(this, Double.NaN);
		this.values = new TreeSet<>();
	}

	@Override
	public GSEnumDataType getType() {
		return GSEnumDataType.Continue;
	}

	// -------------------- SETTERS & GETTER CAPACITIES -------------------- //

	@Override
	public ContinuousValue addValue(String value) {
		if(!gsdp.getValueType(value).isNumericValue())
			throw new IllegalArgumentException("The string value "+value+" does not feet a "
					+ "continued value template");

		double currentVal = gsdp.getDouble(value);
		if(currentVal < min || currentVal > max)
			throw new IllegalArgumentException("Proposed value "+currentVal+" is "
					+ (currentVal < min ? "below" : "beyond") + " given bound ("
					+ (currentVal < min ? min : max) + ")");

		ContinuousValue iv = null;
		try {
			iv = getValue(value);
		} catch (NullPointerException e) {
			iv = new ContinuousValue(this, currentVal);
			values.add(iv);
		}
		return iv;
	}

	@Override
	public ContinuousValue getValue(String value) throws NullPointerException {
		Optional<ContinuousValue> opValue = values.stream()
				.filter(val -> val.getActualValue() == gsdp.getDouble(value)).findAny();
		if(opValue.isPresent())
			return opValue.get();
		throw new NullPointerException("The string value "+value+" is not comprise "
				+ "in the value space "+this.toString());
	}

	@Override
	public Set<ContinuousValue> getValues(){
		return Collections.unmodifiableSet(values);
	}

	@Override
	public ContinuousValue getEmptyValue() {
		return emptyValue;
	}

	@Override
	public void setEmptyValue(String value) {
		try {
			this.emptyValue = getValue(value);
		} catch (NullPointerException npe) {
			try {
				this.emptyValue = new ContinuousValue(this, gsdp.getDouble(value));
			} catch (Exception e) {
				// IF value == null or value is not a parsable double
				// just keep with default empty value
			}
		}
	}

	@Override
	public boolean isValidCandidate(String value) {
		if(!gsdp.getValueType(value).isNumericValue() 
				|| gsdp.getDouble(value) < min || gsdp.getDouble(value) > max)
			return false;
		return true;
	}

	@Override
	public IAttribute<ContinuousValue> getAttribute() {
		return attribute;
	}

	// ----------------------------------------------------- //

	@JsonProperty("max")
	public double getMax() {
		return max;
	}

	public void setMax(double max) {
		this.max = max;
	}

	@JsonProperty("min")
	public double getMin()	{
		return min;
	}

	public void setMin(double min) {
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
