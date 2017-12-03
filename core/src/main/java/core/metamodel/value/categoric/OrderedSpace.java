package core.metamodel.value.categoric;

import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonIgnore;

import core.metamodel.attribute.IAttribute;
import core.metamodel.attribute.IValueSpace;
import core.metamodel.value.categoric.template.GSCategoricTemplate;
import core.util.data.GSEnumDataType;

/**
 * TODO: javadoc
 * 
 * @author kevinchapuis
 *
 */
public class OrderedSpace implements IValueSpace<OrderedValue> {

	// Generic purpose comparator. Ordered value does not implement comparable because
	// they only be compared within a given ordered space
	@JsonIgnore
	private static Comparator<OrderedValue> comp = new Comparator<OrderedValue>() {
		@Override
		public int compare(OrderedValue o1, OrderedValue o2) {return o1.compareTo(o2);}
	}; 

	private TreeSet<OrderedValue> values;
	private OrderedValue emptyValue;

	private IAttribute<OrderedValue> attribute;

	private GSCategoricTemplate template;

	public OrderedSpace(IAttribute<OrderedValue> attribute, GSCategoricTemplate template){
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

	/**
	 * 
	 * @param order
	 * @param value
	 * @return
	 * @throws IllegalArgumentException
	 */
	public OrderedValue addValue(int order, String value) throws IllegalArgumentException {
		OrderedValue ov = null;
		try {
			ov = this.getValue(value);
			if(ov.getOrder() != order)
				throw new IllegalArgumentException("Ordered value "+value+" already exists with order "+ov.getOrder());
		} catch (NullPointerException e) {
			ov = new OrderedValue(this, value, order);
			this.values.add(ov);
		}
		return ov;
	}

	@Override
	public Set<OrderedValue> getValues(){
		return Collections.unmodifiableSet(values);
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
		try {
			this.emptyValue = getValue(value);
		} catch (NullPointerException e) {
			this.emptyValue = new OrderedValue(this, value, 0);
		}
	}

	@Override
	public IAttribute<OrderedValue> getAttribute() {
		return attribute;
	}

	/**
	 * Gives the template used to elaborate proper formated value for this value space
	 * 
	 * @return
	 */
	public GSCategoricTemplate getCategoricTemplate() {
		return template;
	}

	// ---------------------------------------------- //

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = this.getHashCode();
		result = prime * result + template.hashCode();
		return result;

	}

	@Override
	public boolean equals(Object obj) {
		return this.isEqual(obj) && 
				obj == null || this == null ? false : 
					this.template.equals(((OrderedSpace)obj).getCategoricTemplate());
	}

}
