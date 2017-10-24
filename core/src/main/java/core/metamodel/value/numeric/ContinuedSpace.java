package core.metamodel.value.numeric;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.TreeSet;

import core.metamodel.IAttribute;
import core.metamodel.value.IValueSpace;
import core.util.data.GSDataParser;
import core.util.data.GSEnumDataType;

public class ContinuedSpace implements IValueSpace<ContinuedValue> {

	private static GSDataParser gsdp = new GSDataParser();
	
	private ContinuedValue emptyValue;
	private TreeSet<ContinuedValue> values;
	private double min, max;
	
	private IAttribute<ContinuedValue> attribute;
	
	public ContinuedSpace(IAttribute<ContinuedValue> attribute) {
		this.attribute = attribute;
		this.emptyValue = new ContinuedValue(this, Double.NaN);
		this.values = new TreeSet<>();
	}
	
	@Override
	public GSEnumDataType getType() {
		return GSEnumDataType.Continue;
	}

	// -------------------- SETTERS & GETTER CAPACITIES -------------------- //
	
	@Override
	public ContinuedValue addValue(String value) {
		if(!gsdp.getValueType(value).isNumericValue())
			throw new IllegalArgumentException("The string value "+value+" does not feet a "
					+ "continued value template");
		
		double currentVal = gsdp.getDouble(value);
		if(currentVal < min || currentVal > max)
			throw new IllegalArgumentException("Proposed value "+value+" is "
					+ (currentVal < min ? "below" : "beyond") + " given bound ("
							+ (currentVal < min ? min : max) + ")");
		
		ContinuedValue iv = null;
		try {
			iv = getValue(value);
		} catch (NullPointerException e) {
			iv = new ContinuedValue(this, currentVal);
			values.add(iv);
		}
		return iv;
	}
	
	@Override
	public boolean add(ContinuedValue e) {
		if(values.contains(e) || 
				e.getActualValue() < min || e.getActualValue() > max)
			return false;
		this.addValue(e.getStringValue());
		return true;
	}
	
	@Override
	public boolean addAll(Collection<? extends ContinuedValue> c) {
		boolean res = false;
		for(ContinuedValue dv : c)
			if(add(dv))
				res = true;
		return res;
	}
	
	@Override
	public ContinuedValue getValue(String value) throws NullPointerException {
		Optional<ContinuedValue> opValue = values.stream()
				.filter(val -> val.getActualValue() == gsdp.getDouble(value)).findAny();
		if(opValue.isPresent())
			return opValue.get();
		throw new NullPointerException("The string value "+value+" is not comprise "
				+ "in the value space "+this.toString());
	}
	
	@Override
	public ContinuedValue getEmptyValue() {
		return emptyValue;
	}

	@Override
	public void setEmptyValue(String value) {
		try {
			this.emptyValue = new ContinuedValue(this, gsdp.getDouble(value));
		} catch (Exception e) {
			// IF value == null or value is not a parsable double
			// just keep with default empty value
		}
	}

	@Override
	public boolean isValidCandidate(String value) {
		if(!gsdp.getValueType(value).isNumericValue() 
				|| gsdp.getDouble(value) < min || gsdp.getDouble(value) > max)
			return false;
		return true;
	}
	
	// ---------------------------------------------------------------------- //

	@Override
	public IAttribute<ContinuedValue> getAttribute() {
		return attribute;
	}

	@Override
	public String toString(){
		return this.getAttribute().getAttributeName()+"_"+this.getType();
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
	public Iterator<ContinuedValue> iterator() {
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
