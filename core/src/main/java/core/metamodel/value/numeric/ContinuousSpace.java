package core.metamodel.value.numeric;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonProperty;

import core.metamodel.attribute.IAttribute;
import core.metamodel.attribute.IValueSpace;
import core.metamodel.value.IValue;
import core.util.data.GSDataParser;
import core.util.data.GSEnumDataType;

/**
 * Encapsulates a set of double value
 * 
 * WARNING: inner structure is not time efficient, also for fast use of {@link ContinuousValue}
 * try to use {@link #getInstanceValue(String)} or {@link #proposeValue(String)}, that do not
 * store {@link ContinuousValue} in a collection
 * 
 * @author kevinchapuis
 *
 */
public class ContinuousSpace implements IValueSpace<ContinuousValue> {

	private static GSDataParser gsdp = new GSDataParser();

	private ContinuousValue emptyValue;
	private TreeMap<Double, ContinuousValue> values;

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
		this.values = new TreeMap<>();
	}

	@Override
	public GSEnumDataType getType() {
		return GSEnumDataType.Continue;
	}
	
	@Override
	public ContinuousValue getInstanceValue(String value, String label) {
		double currentVal = gsdp.getDouble(value);
		if(currentVal < min || currentVal > max)
			throw new IllegalArgumentException("Proposed value "+currentVal+" is "
					+ (currentVal < min ? "below" : "beyond") + " given bound ("
					+ (currentVal < min ? min : max) + ")");
		return new ContinuousValue(this, currentVal);
	}

	@Override
	public ContinuousValue proposeValue(String value, String label) {
		return new ContinuousValue(this, gsdp.getDouble(value));
	}
	
	// -------------------- SETTERS & GETTER CAPACITIES -------------------- //

	@Override
	public ContinuousValue addValue(String value) {
		ContinuousValue iv = getValue(value);
		if(value == null) {
			iv = this.getInstanceValue(value, value);
			values.put(iv.getActualValue(), iv);
		}
		return iv;
	}
	
	/**
	 * For continuous space, label is ignored.
	 */
	@Override
	public ContinuousValue addValue(String value, String label) throws IllegalArgumentException {
		return addValue(value);
	}

	@Override
	public ContinuousValue getValue(String value) throws NullPointerException {
		return values.get(gsdp.getDouble(value));
	}

	@Override
	public Set<ContinuousValue> getValues(){
		return new HashSet<>(values.values());
	}
	
	@Override
	public boolean contains(IValue value) {
		if(!value.getClass().equals(ContinuousValue.class))
			return false;
		return values.containsValue(value);
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

	@Override
	public boolean contains(String valueStr) {
		return values.values().stream().anyMatch(v -> v.getStringValue().equals(valueStr));
	}
	
	@Override
	public boolean containsAllLabels(Collection<String> valuesStr) {
		return this.values.values()
				.stream()
				.allMatch(val -> valuesStr.contains(val.getStringValue()));
	}

}
