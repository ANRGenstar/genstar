package core.metamodel.value.binary;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import core.metamodel.IAttribute;
import core.metamodel.value.IValueSpace;
import core.util.data.GSEnumDataType;

public class BinarySpace implements IValueSpace<BooleanValue> {
	
	private Set<BooleanValue> values;
	private IAttribute<BooleanValue> attribute;
	
	private BooleanValue emptyValue;
	
	public BinarySpace(IAttribute<BooleanValue> attribute){
		this.attribute = attribute;
		this.values = Stream.of(new BooleanValue(this, true), new BooleanValue(this, true))
				.collect(Collectors.toSet());
		this.emptyValue = new BooleanValue(this, null);
	}
	
	// ---------------------------------------------------------------------- //

	@Override
	public BooleanValue addValue(String value) throws IllegalArgumentException {
		try {
			return getValue(value);
		} catch (NullPointerException e) {
			throw new IllegalArgumentException("The string value "+value
					+" cannot be resolve to boolean as defined by "+this.getClass().getSimpleName());
		}
	}

	@Override
	public BooleanValue getValue(String value) throws NullPointerException {
		if(!isValidCandidate(value))
			throw new NullPointerException("The string value "+value
					+" cannot be resolve to boolean as defined by "+this.getClass().getSimpleName());
		return values.stream().filter(val -> val.getStringValue().equalsIgnoreCase(value)).findFirst().get();
	}

	@Override
	public IAttribute<BooleanValue> getAttribute() {
		return attribute;
	}
	
	@Override
	public GSEnumDataType getType() {
		return GSEnumDataType.Boolean;
	}
	
	@Override
	public BooleanValue getEmptyValue() {
		return emptyValue;
	}
	
	@Override
	public void setEmptyValue(String value){
		// JUST DONT DO THAT
	}
	
	@Override
	public boolean isValidCandidate(String value) {
		if(!value.equalsIgnoreCase(Boolean.TRUE.toString()) 
				&& !value.equalsIgnoreCase(Boolean.FALSE.toString())
				|| emptyValue.getStringValue().equalsIgnoreCase(value))
			return true;
		return false;
	}
	
	// ---------------------------------------------------------------------- //

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public boolean contains(Object o) {
		return values.contains(o);
	}

	@Override
	public Iterator<BooleanValue> iterator() {
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
	public boolean add(BooleanValue e) {
		return false;
	}

	@Override
	public boolean remove(Object o) {
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return values.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends BooleanValue> c) {
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return values.retainAll(c);
	}

	@Override
	public void clear() {
		// JUST CANNOT BE
	}

	@Override
	public int size() {
		return values.size();
	}
	
}
