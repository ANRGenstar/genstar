package core.metamodel.entity;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import core.metamodel.IPopulation;
import core.metamodel.attribute.Attribute;
import core.metamodel.attribute.AttributeFactory;
import core.metamodel.attribute.EmergentAttribute;
import core.metamodel.attribute.IAttribute;
import core.metamodel.value.IValue;
import core.metamodel.value.numeric.IntegerValue;

/**
 * The higher order abstraction for demographic entity. Manage basic attribute / value relationship and parent / children relationship.
 * 
 * TODO: study the possibility of extending {@link IPopulation} - but may be too holonic
 * 
 * @author kevinchapuis
 *
 */
public abstract class ADemoEntity implements IEntity<Attribute<? extends IValue>> { // , IPopulation<ADemoEntity, Attribute<? extends IValue>> {

	/**
	 * The unique identifier of the entity. See {@link EntityUniqueId}
	 */
	private String id = null;
	
	/**
	 * The map of attribute / value ! What characterize the entity: vector of value, one value for each attribute.
	 */
	protected Map<Attribute<? extends IValue>, IValue> attributes;

	
	/**
	 * The type of the agent (like "household" or "building"), 
	 * or null if undefined
	 */
	private String type;
	
	/**
	 * Creates a population entity by defining directly the attribute values
	 * @param attributes
	 */
	public ADemoEntity(Map<Attribute<? extends IValue>, IValue> attributes) {
		this.attributes = attributes;
	}
	
	/**
	 * creates a population entity without defining its population attributes
	 * @param attributes
	 */
	public ADemoEntity() {
		this.attributes = new HashMap<Attribute<? extends IValue>, IValue>();
	}
	
	/**
	 * Clone like constructor.
	 * @param e
	 */
	public ADemoEntity(ADemoEntity e) {
		this.attributes = new HashMap<>(e.getAttributeMap());
		this.type = e.type;
	}
	
	/**
	 * creates a population entity by defining the attributes it will contain without attributing any value
	 * @param attributes
	 */
	public ADemoEntity(Collection<Attribute<IValue>> attributes) {
		this.attributes = attributes.stream().collect(Collectors
				.toMap(Function.identity(), att -> null));
	}
	
	/**
	 * Clone returns a similar population entity whose values might be modified without modifying the 
	 * parent one.
	 */
	public abstract ADemoEntity clone();
	

	@Override
	public final void _setEntityId(String novelid) throws IllegalStateException {
		if (this.id != null)
			throw new IllegalArgumentException("cannot change the identifier of an agent; "+
						"this agent already had id "+this.id+" but we were asked "+
					"to change it for "+novelid);
		this.id = novelid;
	}

	@Override
	public final String getEntityId() throws IllegalStateException {
		if (this.id == null)
			throw new IllegalStateException("no id is defined yet for agent "+this.toString());
		return this.id;
	}
	
	@Override
	public final boolean _hasEntityId() {
		return this.id != null;
	}

	
	// ---------------------------------------------------------------------- //
	
	/** 
	 * sets the value for the attribute or updates this value
	 * @param attribute
	 * @param value
	 */
	public void setAttributeValue(Attribute<? extends IValue> attribute, IValue value) {
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

		Optional<Attribute<? extends IValue>> opAtt = attributes.keySet().stream()
				.filter(att -> att.getAttributeName().equals(attributeName)).findAny();
		
		if(!opAtt.isPresent())
			throw new IllegalArgumentException("there is no attribute named "+attributeName+" defined for this entity");
				
		this.attributes.put(opAtt.get(), value);
	}
	
	// ----------------------------------------------------------------------- //
	
	@Override
	public boolean hasAttribute(Attribute<? extends IValue> a) {
		return attributes.containsKey(a);
	}

	@Override
	public IValue getValueForAttribute(Attribute<? extends IValue> attribute) {
		return attributes.get(attribute);
	}

	@Override
	public IValue getValueForAttribute(String property) {
		for (Attribute<? extends IValue> att :this.attributes.keySet()) {
			if (att.getAttributeName().equals(property)) return attributes.get(att);
		}
		return null;
	}
	
	@Override
	public Map<Attribute<? extends IValue>, IValue> getAttributeMap() {
		return Collections.unmodifiableMap(attributes);
	}
	
	@Override
	public Collection<Attribute<? extends IValue>> getAttributes() {
		return attributes.keySet();
	}

	@Override
	public Collection<IValue> getValues() {
		return Collections.unmodifiableCollection(attributes.values());
	}
	
	@Override
	public String toString() {
		return "Entity ["+this.id+"] "+attributes.entrySet().stream().map(e -> e.getKey().getAttributeName()
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
	
	/**
	 * The parent entity, if any
	 */
	private IEntity<? extends IAttribute<? extends IValue>> parent = null;
	
	/**
	 * The set of children, if any. 
	 * Else remains null (lazy creation)
	 */
	private Set<IEntity<? extends IAttribute<? extends IValue>>> children = null;
	
	public static EmergentAttribute<IntegerValue, ADemoEntity, ?> SIZE_ATTRIBUTE = AttributeFactory.getFactory()
			.createCountAttribute("SIZE ATTRIBUTE", null);
	
	@Override
	public final boolean hasParent() {
		return parent != null;
	}

	@Override
	public final IEntity<? extends IAttribute<? extends IValue>> getParent() {
		return parent;
	}

	@Override
	public final void setParent(IEntity<? extends IAttribute<? extends IValue>> e) {
		this.parent = e;		
	}
	
	@Override
	public final boolean hasChildren() {
		return children != null && !children.isEmpty();
	}

	@Override
	public final IntegerValue getCountChildren() {
		if (children == null)
			return SIZE_ATTRIBUTE.getValueSpace().proposeValue("0");
		return SIZE_ATTRIBUTE.getEmergentValue(this, null);
	}

	@Override
	public final Set<IEntity<? extends IAttribute<? extends IValue>>> getChildren() {
		if (children == null)
			return Collections.emptySet();
		return Collections.unmodifiableSet(children);
	}

	@Override
	public final void addChild(IEntity<? extends IAttribute<? extends IValue>> e) {
		if (children == null)
			children = new HashSet<>();
		children.add(e);
	}

	@Override
	public final void addChildren(Collection<IEntity<? extends IAttribute<? extends IValue>>> e) {
		if (children == null)
			children = new HashSet<>();
		children.addAll(e);
	}

	
}
