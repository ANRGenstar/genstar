package core.metamodel.value.numeric;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import core.metamodel.attribute.IAttribute;
import core.metamodel.attribute.IValueSpace;
import core.metamodel.value.IValue;
import core.metamodel.value.numeric.RangeValue.RangeBound;
import core.metamodel.value.numeric.template.GSRangeTemplate;
import core.util.data.GSDataParser;
import core.util.data.GSDataParser.NumMatcher;
import core.util.data.GSEnumDataType;
import core.util.excpetion.GSIllegalRangedData;

/**
 * Encapsulate pair of number that represents bottom and top value of a range. It also
 * provide a template reader to convert range value to original string and back to range value.
 * 
 * WARNING: when lower and upper bounds are not specified, max and min integer value are used
 * 
 * @see GSRangeTemplate
 * 
 * @author kevinchapuis
 *
 */
public class RangeSpace implements IValueSpace<RangeValue> {
	
	private static GSDataParser gsdp = new GSDataParser();
	
	private IAttribute<RangeValue> attribute;
	
	private GSRangeTemplate rt;
	private Number min, max;

	private List<RangeValue> values;
	private RangeValue emptyValue;
	
	/**
	 * 
	 * @param attribute
	 * @param rt
	 */
	public RangeSpace(IAttribute<RangeValue> attribute, GSRangeTemplate rt){
		this(attribute, rt, Integer.MIN_VALUE, Integer.MAX_VALUE);
	}
	
	public RangeSpace(IAttribute<RangeValue> attribute, List<String> ranges,
			Number minValue, Number maxValue) throws GSIllegalRangedData{
		this(attribute, gsdp.getRangeTemplate(ranges, GSDataParser.DEFAULT_NUM_MATCH, NumMatcher.getDefault()),
				Integer.MIN_VALUE, Integer.MAX_VALUE);
	}
	
	public RangeSpace(IAttribute<RangeValue> attribute, GSRangeTemplate rt, 
			Number minValue, Number maxValue) {
		this.attribute = attribute;
		this.rt = rt;
		this.min = minValue;
		this.max = maxValue;
		this.values = new ArrayList<>();
		this.emptyValue = new RangeValue(this, Double.NaN, Double.NaN);
	}
	
	// ------------------------------------------------------- //
	
	/**
	 * Return the range formatter that is able to transpose range value to string and way back
	 * 
	 * @return
	 */
	public GSRangeTemplate getRangeTemplate(){
		return rt;
	}
	
	@Override
	public RangeValue getInstanceValue(String value) {
		if(!rt.isValideRangeCandidate(value))
			throw new IllegalArgumentException("The string value "+value+" does not feet defined "
					+ "range "+rt);
		
		List<Number> currentVal = null;
		currentVal = gsdp.getNumbers(value, rt.getNumerciMatcher());
		if(currentVal.stream().anyMatch(d -> d.doubleValue() < min.doubleValue()) || 
				currentVal.stream().anyMatch(d -> d.doubleValue() > max.doubleValue()))
			throw new IllegalArgumentException("Proposed values "+value+" are "
					+ (currentVal.stream().anyMatch(d -> d.doubleValue() < min.doubleValue()) ? "below" : "beyond") + " given bound ("
							+ (currentVal.stream().anyMatch(d -> d.doubleValue() < min.doubleValue()) ? min : max) + ")");
		return currentVal.size() == 1 ? 
				(rt.getBottomTemplate(currentVal.get(0)).equals(value) ? 
						new RangeValue(this, currentVal.get(0), RangeBound.LOWER) :
							new RangeValue(this, currentVal.get(0), RangeBound.UPPER)) :
			new RangeValue(this, currentVal.get(0), currentVal.get(1));
	}
	
	@Override
	public RangeValue proposeValue(String value) {
		return getInstanceValue(value);
	}
	
	// -------------------- SETTERS & GETTER CAPACITIES -------------------- //

	@Override
	public RangeValue addValue(String value) throws IllegalArgumentException {
		RangeValue iv = null;
		try {
			iv = getValue(value);
		} catch (NullPointerException e) {	
			iv = this.getInstanceValue(value);
			this.values.add(iv);
		}
		return iv;
	}

	@Override
	public RangeValue getValue(String value) throws NullPointerException {
		Optional<RangeValue> opValue = values.stream()
				.filter(v -> v.getStringValue().equals(value)).findAny();
		if(opValue.isPresent())
			return opValue.get();
		throw new NullPointerException("The string value \""+value+"\" is not comprise "
				+ "in the value space "+this.toPrettyString());
	}
	
	@Override
	public Set<RangeValue> getValues(){
		return new HashSet<>(values);
	}
	
	@Override
	public boolean contains(IValue value) {
		if(!value.getClass().equals(RangeValue.class))
			return false;
		return values.contains(value);
	}
	
	@Override
	public RangeValue getEmptyValue() {
		return emptyValue;
	}

	@Override
	public void setEmptyValue(String value) {
		if(rt.isValideRangeCandidate(value)){
			try {
				getValue(value);
			} catch (Exception e) {
				List<Double> currentVal = null;
				try {
					currentVal = gsdp.getRangedDoubleData(value, rt.getNumerciMatcher());
				} catch (GSIllegalRangedData e1) {
					// TODO Auto-generated catch block
					throw new IllegalArgumentException("SHOULD NOT HAPPEN");
				}
				this.emptyValue = currentVal.size() == 1 ? 
						(rt.getBottomTemplate(currentVal.get(0)).equals(value) ? 
								new RangeValue(this, currentVal.get(0), RangeBound.LOWER) :
									new RangeValue(this, currentVal.get(0), RangeBound.UPPER)) :
					new RangeValue(this, currentVal.get(0), currentVal.get(1));
			}
		}
	}
	
	@Override
	public boolean isValidCandidate(String value){
		return rt.isValideRangeCandidate(value);
	}

	@Override
	public GSEnumDataType getType() {
		return GSEnumDataType.Range;
	}

	@Override
	public IAttribute<RangeValue> getAttribute() {
		return this.attribute;
	}
	
	/**
	 * Get the minimum value
	 * @return
	 */
	public Number getMin() {return min;}
	
	/**
	 * Get the maximum value
	 * @return
	 */
	public Number getMax() {return max;}
	
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
