package core.metamodel.attribute.geographic;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import core.metamodel.attribute.IAttribute;
import core.metamodel.attribute.IValueSpace;
import core.metamodel.value.IValue;
import core.util.data.GSDataParser;
import core.util.data.GSEnumDataType;

public class GeographicValueSpace<V extends IValue> implements IValueSpace<V> {

	private IValueSpace<V> innerValueSpace;
	private Set<V> noDataValues;
	
	public GeographicValueSpace(IValueSpace<V> innerValueSpace){
		this.innerValueSpace = innerValueSpace;
		this.noDataValues = new HashSet<>();
	}
	
	public GeographicValueSpace(IValueSpace<V> innerValueSpace,
			Collection<V> noDataValues){
		this.innerValueSpace = innerValueSpace;
		this.noDataValues = new HashSet<>(noDataValues);
	}
	
	// ------------------- GEO RELATED CONTRACT ------------------- //
	
	/**
	 * Add a list of excluded values
	 * 
	 * @param asList
	 * @return
	 */
	public boolean addExcludedValues(Collection<V> asList) {
		return this.noDataValues.addAll(asList);
	}
	
	/**
	 * Get a numerical representation of a given value
	 * 
	 * @param val
	 * @return
	 */
	public Number getNumericValue(V val) {
		if(!val.getType().isNumericValue())
			return Double.NaN;
		return new GSDataParser().parseNumber(val.getStringValue());
	}

	// ---------------------- ADDER CONTRACT ---------------------- //
	
	@Override
	public V addValue(String value) throws IllegalArgumentException {
		V val = null;
		try {
			val = this.getValue(value);
		} catch (NullPointerException npe) {
			val = innerValueSpace.addValue(value);
		} catch (IllegalArgumentException iae){
			iae.printStackTrace();
			System.exit(1);
		}
		return val;
	}

	@Override
	public V getValue(String value) throws NullPointerException {
		if(noDataValues.stream().anyMatch(val -> val.getStringValue().equals(value)))
			throw new IllegalArgumentException(value+" has been defined as a no data value");
		return innerValueSpace.getValue(value);
	}
	
	@Override
	public boolean add(V arg0) {
		return innerValueSpace.add(arg0);
	}

	@Override
	public boolean addAll(Collection<? extends V> arg0) {
		return innerValueSpace.addAll(arg0);
	}
	
	// ------------------------------------------------------------ //

	@Override
	public boolean contains(Object arg0) {
		return innerValueSpace.contains(arg0);
	}

	@Override
	public boolean containsAll(Collection<?> arg0) {
		return innerValueSpace.containsAll(arg0);
	}

	@Override
	public boolean isEmpty() {
		return innerValueSpace.isEmpty();
	}

	@Override
	public Iterator<V> iterator() {
		return innerValueSpace.iterator();
	}

	@Override
	public boolean remove(Object arg0) {
		return innerValueSpace.remove(arg0);
	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
		return innerValueSpace.removeAll(arg0);
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		return innerValueSpace.retainAll(arg0);
	}

	@Override
	public int size() {
		return innerValueSpace.size();
	}

	@Override
	public Object[] toArray() {
		return innerValueSpace.toArray();
	}

	@Override
	public <T> T[] toArray(T[] arg0) {
		return innerValueSpace.toArray(arg0);
	}
	
	@Override
	public void clear() {
		innerValueSpace.clear();
		noDataValues.clear();
	}
	
	// ---------------------------------------------------- //

	@Override
	public boolean isValidCandidate(String value) {
		return innerValueSpace.isValidCandidate(value);
	}

	@Override
	public GSEnumDataType getType() {
		return innerValueSpace.getType();
	}

	@Override
	public V getEmptyValue() {
		return innerValueSpace.getEmptyValue();
	}

	@Override
	public void setEmptyValue(String value) {
		innerValueSpace.setEmptyValue(value);
	}

	@Override
	public IAttribute<V> getAttribute() {
		return innerValueSpace.getAttribute();
	}
	
}
