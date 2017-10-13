package core.metamodel.value.numeric;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import core.metamodel.IAttribute;
import core.metamodel.value.IValueSpace;
import core.metamodel.value.numeric.RangeValue.RangeBound;
import core.util.data.GSDataParser;
import core.util.data.GSDataParser.NumMatcher;
import core.util.data.GSEnumDataType;
import core.util.data.GSRangeTemplate;
import core.util.excpetion.GSIllegalRangedData;

public class RangeSpace implements IValueSpace<RangeValue> {
	
	public static GSDataParser gsdp = new GSDataParser();
	
	private IAttribute<RangeValue> attribute;
	
	private GSRangeTemplate rt;
	private int min, max;

	private List<RangeValue> values;
	private RangeValue emptyValue;
	
	public RangeSpace(IAttribute<RangeValue> attribute, GSRangeTemplate rt){
		this(attribute, rt, Integer.MIN_VALUE, Integer.MAX_VALUE);
	}
	
	public RangeSpace(IAttribute<RangeValue> attribute, List<String> ranges,
			int minValue, int maxValue) throws GSIllegalRangedData{
		this(attribute, gsdp.getRangeTemplate(ranges, GSDataParser.DEFAULT_NUM_MATCH, NumMatcher.getDefault()),
				Integer.MIN_VALUE, Integer.MAX_VALUE);
	}
	
	public RangeSpace(IAttribute<RangeValue> attribute, GSRangeTemplate rt, 
			int minValue, int maxValue) {
		this.attribute = attribute;
		this.rt = rt;
		this.min = minValue;
		this.max = maxValue;
		this.emptyValue = new RangeValue(this, Double.NaN, Double.NaN);
	}
	
	/**
	 * Return the range formatter that is able to transpose range value to string and way back
	 * 
	 * @return
	 */
	public GSRangeTemplate getRangeTemplate(){
		return rt;
	}
	
	// -------------------- SETTERS & GETTER CAPACITIES -------------------- //

	@Override
	public RangeValue addValue(String value) throws IllegalArgumentException {
		if(!rt.isValideRangeCandidate(value))
			throw new IllegalArgumentException("The string value "+value+" does not feet defined "
					+ "range "+rt);
		
		List<Double> currentVal = null;
		try {
			currentVal = gsdp.getRangedDoubleData(value, rt.getNumerciMatcher());
		} catch (GSIllegalRangedData e1) {
			// TODO Auto-generated catch block
			throw new IllegalArgumentException("SHOULD NOT HAPPEN");
		}
		if(currentVal.stream().anyMatch(d -> d < min) || 
				currentVal.stream().anyMatch(d -> d > max))
			throw new IllegalArgumentException("Proposed values "+value+" are "
					+ (currentVal.stream().anyMatch(d -> d < min) ? "below" : "beyond") + " given bound ("
							+ (currentVal.stream().anyMatch(d -> d < min) ? min : max) + ")");
		
		RangeValue iv = null;
		try {
			iv = getValue(value);
		} catch (NullPointerException e) {	
			iv = currentVal.size() == 1 ? 
					(rt.getLowerTemplate(currentVal.get(0)).equals(value) ? 
							new RangeValue(this, currentVal.get(0), RangeBound.LOWER) :
								new RangeValue(this, currentVal.get(0), RangeBound.UPPER)) :
				new RangeValue(this, currentVal.get(0), currentVal.get(1));
			values.add(iv);
		}
		return iv;
	}
	
	@Override
	public boolean add(RangeValue e) {
		if(values.contains(e) ||
				Arrays.asList(e.getActualValue()).stream()
					.anyMatch(num -> num.doubleValue() < min || num.doubleValue() > max) 
				|| !rt.isValideRangeCandidate(e.getStringValue()))
			return false;
		return values.add(e);
	}
	
	@Override
	public boolean addAll(Collection<? extends RangeValue> c) {
		boolean res = false;
		for(RangeValue rv : c)
			if(add(rv))
				res = true;
		return res;
	}

	@Override
	public RangeValue getValue(String value) throws NullPointerException {
		Optional<RangeValue> opValue = values.stream()
				.filter(v -> v.getStringValue().equals(value)).findAny();
		if(opValue.isPresent())
			return opValue.get();
		throw new NullPointerException("The string value "+value+" is not comprise "
				+ "in the value space "+this.toString());
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
						(rt.getLowerTemplate(currentVal.get(0)).equals(value) ? 
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
	
	// ---------------------------------------------------------------------- //

	@Override
	public GSEnumDataType getType() {
		return GSEnumDataType.Range;
	}

	@Override
	public IAttribute<RangeValue> getAttribute() {
		return this.attribute;
	}

	@Override
	public int size() {
		return values.size();
	}

	@Override
	public boolean isEmpty() {
		return values.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return values.contains(o);
	}

	@Override
	public Iterator<RangeValue> iterator() {
		return values.iterator();
	}

	@Override
	public Object[] toArray() {
		return values.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return values.toArray(a);
	}

	@Override
	public boolean remove(Object o) {
		return values.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return values.containsAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return values.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return values.retainAll(c);
	}

	@Override
	public void clear() {
		values.clear();
	}

}
