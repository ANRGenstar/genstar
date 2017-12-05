package core.metamodel.value.numeric;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

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
	public ContinuousValue getInstanceValue(String value) {
		double currentVal = gsdp.getDouble(value);
		if(currentVal < min || currentVal > max)
			throw new IllegalArgumentException("Proposed value "+currentVal+" is "
					+ (currentVal < min ? "below" : "beyond") + " given bound ("
					+ (currentVal < min ? min : max) + ")");
		return new ContinuousValue(this, currentVal);
	}

	@Override
	public ContinuousValue proposeValue(String value) {
		return new ContinuousValue(this, gsdp.getDouble(value));
	}
	
	// -------------------- SETTERS & GETTER CAPACITIES -------------------- //

	@Override
	public ContinuousValue addValue(String value) {
		ContinuousValue iv = getValue(value);
		if(value == null) {
			iv = this.getInstanceValue(value);
			values.put(iv.getActualValue(), iv);
		}
		return iv;
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