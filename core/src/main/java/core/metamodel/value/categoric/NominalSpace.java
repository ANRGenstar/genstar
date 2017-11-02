package core.metamodel.value.categoric;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import core.metamodel.attribute.IAttribute;
import core.metamodel.attribute.IValueSpace;
import core.metamodel.value.categoric.template.GSCategoricTemplate;
import core.util.data.GSEnumDataType;

public class NominalSpace implements IValueSpace<NominalValue> {

	private IAttribute<NominalValue> attribute; 
	
	private Set<NominalValue> values;
	private NominalValue emptyValue;
	
	private GSCategoricTemplate ct;
	
	public NominalSpace(IAttribute<NominalValue> attribute,
			GSCategoricTemplate ct){
		this.attribute = attribute;
		this.emptyValue = new NominalValue(this, null);
		this.ct = ct;
	}
	
	@Override
	public GSEnumDataType getType() {
		return GSEnumDataType.Nominal;
	}
	
	@Override
	public boolean isValidCandidate(String value) {
		return true;
	}
	
	// -------------------- SETTERS & GETTER CAPACITIES -------------------- //
	
	@Override
	public NominalValue addValue(String value) throws IllegalArgumentException {
		NominalValue nv = null;
		try {
			nv = this.getValue(value);
		} catch (NullPointerException e) {
			nv = new NominalValue(this, ct.getFormatedString(value));
		}
		return nv;
	}
	
	@Override
	public boolean add(NominalValue e) {
		if(values.contains(e) ||
				!ct.getFormatedString(e.getStringValue()).equals(e.getStringValue()))
			return false;
		this.addValue(e.getStringValue());
		return true;
	}
	
	@Override
	public boolean addAll(Collection<? extends NominalValue> c) {
		boolean res = false;
		for(NominalValue nv : c)
			if(this.add(nv) && !res)
				res = true;
		return res;
	}

	@Override
	public NominalValue getValue(String value) throws NullPointerException {
		String formatedValue = ct.getFormatedString(value);
		Optional<NominalValue> opValue = values.stream()
				.filter(v -> v.getStringValue().equals(formatedValue)).findAny();
		if(opValue.isPresent())
			return opValue.get();
		throw new NullPointerException("The string value "+value+" is not comprise "
				+ "in the value space "+this.toString());
	}
	
	@Override
	public NominalValue getEmptyValue() {
		return emptyValue;
	}
	
	@Override
	public void setEmptyValue(String value){
		this.emptyValue = new NominalValue(this, value);
	}
	
	// ---------------------------------------------------------------------- //

	@Override
	public IAttribute<NominalValue> getAttribute() {
		return attribute;
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
	public Iterator<NominalValue> iterator() {
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
