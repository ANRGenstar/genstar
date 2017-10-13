package core.metamodel.value.categoric;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;

import core.metamodel.IAttribute;
import core.metamodel.value.IValueSpace;
import core.metamodel.value.categoric.template.GSCategoricTemplate;
import core.util.data.GSEnumDataType;

public class OrderedSpace implements IValueSpace<OrderedValue> {

	// Generic purpose comparator. Ordered value does not implement comparable because
	// they only be compared within a given ordered space
	private static Comparator<OrderedValue> comp = new Comparator<OrderedValue>() {
		@Override
		public int compare(OrderedValue o1, OrderedValue o2) {return o1.compareTo(o2);}
	}; 
	
	private OrderedValue emptyValue;
	private TreeSet<OrderedValue> values;
	private IAttribute<OrderedValue> attribute;

	private GSCategoricTemplate template;
	
	public OrderedSpace(IAttribute<OrderedValue> attribute,
			GSCategoricTemplate template){
		this.values = new TreeSet<>(comp);
		this.attribute = attribute;
		this.template = template;
		this.emptyValue = new OrderedValue(this, null, 0);
	}
	
	public int compare(OrderedValue referent, OrderedValue compareTo) {
		return referent.compareTo(compareTo);
	}

	@Override
	public GSEnumDataType getType() {
		return GSEnumDataType.Order;
	}
	
	@Override
	public boolean isValidCandidate(String value){
		return true;
	}

	// ------------------------ SETTERS & ADDER CAPACITIES ------------------------ //
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Whenever this method is called, value is added at the end of the ordered collection.
	 * If one wants to specify order (as int) the value should take use {@link #addValue(int, String)}
	 * 
	 * @param value
	 * @return
	 * @throws IllegalArgumentException
	 */
	@Override
	public OrderedValue addValue(String value) throws IllegalArgumentException {
		return addValue(values.size()-1, value);
	}
	
	public OrderedValue addValue(int order, String value) throws IllegalArgumentException {
		OrderedValue ov = null;
		try {
			ov = this.getValue(value);
			if(ov.getOrder() != order)
				throw new IllegalArgumentException("Ordered value "+value+" already exists with order "+ov.getOrder());
		} catch (NullPointerException e) {
			ov = new OrderedValue(this, value, order);
			values.add(ov);
		}
		return ov;
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Whenever this method is called, ordered value is added according to its internal order.
	 * In case where two value has same internal order then all previously added value with
	 * equal or upper order value are incremented. Works the same way as {@link List#add(int, Object)}
	 * methods shifting all elements to the right
	 *
	 * @param e
	 * @return
	 */
	@Override
	public boolean add(OrderedValue e) {
		if(values.stream().anyMatch(v -> v.getOrder() == e.getOrder()))
			values.stream().filter(v -> v.getOrder() > e.getOrder())
				.forEach(v -> v.setOrder(v.getOrder()+1));
		return values.add(e);
	}
	
	/**
	 * {@inheritDoc}
	 * @see #addValue(String)
	 * @param c
	 * @return
	 */
	@Override
	public boolean addAll(Collection<? extends OrderedValue> c) {
		boolean res = false;
		for(OrderedValue ov : c)
			if(this.add(ov) && !res)
				res = true;
		return res;
	}

	@Override
	public OrderedValue getValue(String value) throws NullPointerException {
		Optional<OrderedValue> opOv = values.stream().filter(ov -> ov.getStringValue()
				.equals(template.format(value))).findAny();
		if(opOv.isPresent())
			return opOv.get();
		throw new NullPointerException("The string value "+value+" is not comprise "
				+ "in the value space "+this.toString());
	}
	
	@Override
	public OrderedValue getEmptyValue() {
		return emptyValue;
	}

	@Override
	public void setEmptyValue(String value) {
		this.emptyValue = new OrderedValue(this, value, 0);
	}
	
	// ----------------------------------------------------------------------------- //

	@Override
	public IAttribute<OrderedValue> getAttribute() {
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
	public Iterator<OrderedValue> iterator() {
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
