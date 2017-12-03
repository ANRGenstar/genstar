package core.metamodel.entity;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import core.metamodel.attribute.demographic.DemographicAttribute;
import core.metamodel.value.IValue;

public abstract class ADemoEntity implements IEntity<DemographicAttribute<? extends IValue>> {

	private Map<DemographicAttribute<? extends IValue>, IValue> attributes;

	/**
	 * The type of the agent (like "household" or "building"), 
	 * or null if undefined
	 */
	private String type;
	
	/**
	 * Creates a population entity by defining directly the attribute values
	 * @param attributes
	 */
	public ADemoEntity(Map<DemographicAttribute<? extends IValue>, IValue> attributes) {
		this.attributes = attributes;
	}
	
	/**
	 * creates a population entity without defining its population attributes
	 * @param attributes
	 */
	public ADemoEntity() {
		this.attributes = new HashMap<DemographicAttribute<? extends IValue>, IValue>();
	}
	

	/**
	 * creates a population entity by defining the attributes it will contain without attributing any value
	 * @param attributes
	 */
	public ADemoEntity(Collection<DemographicAttribute<IValue>> attributes) {
		this.attributes = attributes.stream().collect(Collectors
				.toMap(Function.identity(), att -> null));
	}
	
	/**
	 * Clone returns a similar population entity whose values might be modified without modifying the 
	 * parent one.
	 */
	public abstract ADemoEntity clone();
	
	// ---------------------------------------------------------------------- //
	
	/** 
	 * sets the value for the attribute or updates this value
	 * @param attribute
	 * @param value
	 */
	public void setAttributeValue(DemographicAttribute<? extends IValue> attribute, IValue value) {
		this.attributes.put(attribute, value);
	}


	/** 
	 * sets the value for the attribute or updates this value
	 * @param attributeName
	 * @param value
	 */
	public void setAttributeValue(String attributeName, IValue value) {
			
		if (attributes.isEmpty())
			throw new IllegalArgumentException("there is no attribute defined for this entity");

		Optional<DemographicAttribute<? extends IValue>> opAtt = attributes.keySet().stream()
				.filter(att -> att.getAttributeName().equals(attributeName)).findAny();
		
		if(!opAtt.isPresent())
			throw new IllegalArgumentException("there is no attribute named "+attributeName+" defined for this entity");
				
		this.attributes.put(opAtt.get(), value);
	}
	
	// ----------------------------------------------------------------------- //
	
	@Override
	public boolean hasAttribute(DemographicAttribute<? extends IValue> a) {
		return attributes.containsKey(a);
	}

	@Override
	public IValue getValueForAttribute(DemographicAttribute<? extends IValue> attribute) {
		return attributes.get(attribute);
	}

	@Override
	public IValue getValueForAttribute(String property) {
		return attributes.get(this.getValueForAttribute(property));
	}
	
	@Override
	public Map<DemographicAttribute<? extends IValue>, IValue> getAttributeMap() {
		return Collections.unmodifiableMap(attributes);
	}
	
	@Override
	public Collection<DemographicAttribute<? extends IValue>> getAttributes() {
		return attributes.keySet();
	}

	@Override
	public Collection<IValue> getValues() {
		return Collections.unmodifiableCollection(attributes.values());
	}
	
	@Override
	public String toString() {
		return attributes.entrySet().stream().map(e -> e.getKey().getAttributeName()
				+":"+e.getValue().getStringValue()).collect(Collectors.joining(",\t"));
	}
	

	@Override
	public final boolean hasEntityType() {
		return type != null;
	}

	@Override
	public final String getEntityType() {
		return type;
	}

	@Override
	public void setEntityType(String type) {
		this.type = type;
	}
	
	
	
}
