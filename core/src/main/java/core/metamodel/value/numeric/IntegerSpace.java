package core.metamodel.value.numeric;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import core.metamodel.IAttribute;
import core.metamodel.value.IValueSpace;
import core.util.data.GSDataParser;
import core.util.data.GSEnumDataType;

public class IntegerSpace implements IValueSpace<IntegerValue> {

	private static GSDataParser gsdp = new GSDataParser();
	
	private IntegerValue emptyValue;
	private Set<IntegerValue> values;
	private int min, max;

	private IAttribute<IntegerValue> attribute;
	
	public IntegerSpace(IAttribute<IntegerValue> attribute){
		this(attribute, Integer.MIN_VALUE, Integer.MAX_VALUE);
	}
	
	public IntegerSpace(IAttribute<IntegerValue> attribute, 
			Integer min, Integer max) {
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
		
		int currentVal = gsdp.parseNumber(value).intValue();
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
	public boolean add(IntegerValue e) {
		if(values.contains(e) || 
				e.getActualValue() < min || e.getActualValue() > max)
			return false;
		addValue(e.getStringValue());
		return true;
	}
	
	@Override
	public boolean addAll(Collection<? extends IntegerValue> c) {
		boolean res = false;
		for(IntegerValue iv : c)
			if(add(iv) && !res)
				res = true;
		return res;
	}
	
	@Override
	public IntegerValue getValue(String value) throws NullPointerException {
		Optional<IntegerValue> opValue = values.stream()
				.filter(val -> val.getActualValue() == gsdp.parseNumber(value).intValue())
				.findAny();
		if(opValue.isPresent())
			return opValue.get();
		throw new NullPointerException("The string value "+value+" is not comprise "
				+ "in the value space "+this.toString());
	}
	
	@Override
	public IntegerValue getEmptyValue() {
		return emptyValue;
	}

	@Override
	public void setEmptyValue(String value) {
		try {
			this.emptyValue = new IntegerValue(this, gsdp.getDouble(value).intValue());
		} catch (Exception e) {
			// If value is not a parsable integer or null just leave the
			// default value as it is 
		}
	}
	
	@Override
	public boolean isValidCandidate(String value){
		if(!gsdp.getValueType(value).isNumericValue() 
				|| gsdp.parseNumber(value).intValue() < min 
				|| gsdp.parseNumber(value).intValue() > max)
			return false;
		return true;
	}
	
	// ---------------------------------------------------------------------- //

	@Override
	public GSEnumDataType getType() {
		return GSEnumDataType.Integer;
	}

	@Override
	public IAttribute<IntegerValue> getAttribute() {
		return attribute;
	}
	
	@Override
	public String toString(){
		return this.getAttribute().getAttributeName()+"_"+getType();
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
	public Iterator<IntegerValue> iterator() {
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
	
	@Override
	public int size() {
		return values.size();
	}
	
}
