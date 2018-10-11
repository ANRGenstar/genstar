package core.metamodel.attribute.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import core.metamodel.attribute.Attribute;
import core.metamodel.attribute.IAttribute;
import core.metamodel.entity.IEntity;
import core.metamodel.value.IValue;

/**
 * A vector of attribute with binded set of values and several utility methods to asses comparison with other entities made
 * of attribute and value
 * 
 * @author kevinchapuis
 *
 */
public class AttributeVectorMatcher {

	public static final String ATT_SEPRATOR = " : ";
	public static final CharSequence VAL_SEPRATOR = ",";
	
	Map<IAttribute<? extends IValue>, Set<IValue>> vector;
	
	public AttributeVectorMatcher() {
		this.vector = new HashMap<>();
	}
	
	public AttributeVectorMatcher(IEntity<Attribute<? extends IValue>> entity) {
		this.vector = entity.getAttributeMap().entrySet().stream()
				.collect(Collectors.toMap(
						e -> e.getKey(), 
						e -> new HashSet<IValue>(Arrays.asList(e.getValue()))
						));
	}
	
	public AttributeVectorMatcher(Map<IAttribute<? extends IValue>, Set<IValue>> vector) {
		this.vector = vector;
	}
	
	//-------------------------------------------//
	
	/**
	 * If this attribute vector contains given value. Relies on {@link Collection#contains(Object)} implementation.
	 * @param value
	 * @return
	 */
	public boolean valueMatch(IValue value) {
		return this.values().contains(value);
	}
	
	/**
	 * If this attribute vector contains all values provided in the given collection. Relies on {@link Collection#containsAll(Collection)} implementation.
	 * @param values
	 * @return
	 */
	public boolean valuesMatch(Collection<? extends IValue> values) {
		return this.values().containsAll(values);
	}
	
	/**
	 * If the attribute vector contains each value that characterize entity vector value. Relies on {@link Collection#contains(Object)} implementation.
	 * @param entity
	 * @return
	 */
	public boolean entityMatch(IEntity<? extends IAttribute<? extends IValue>> entity) {
		return entity.getAttributes().containsAll(this.getAttributes()) ? 
				entity.getAttributeMap().entrySet().stream()
					.allMatch(e -> vector.get(e.getKey()).contains(e.getValue())) 
				: false;
	}
	
	/**
	 * Return the Hamming distance between this attribute vector and given entity. It relies on entity level value vector contract, meaning
	 * if the attribute as several value for one attribute, the Hamming distance will be 0 is none are present in the entity and 1 if at least
	 * one is actually in the entity value vector
	 * 
	 * @param entity
	 * @return
	 */
	public int getHammingDistance(IEntity<? extends IAttribute<? extends IValue>> entity) {
		return (int) entity.getAttributes().stream()
				.filter(a -> vector.keySet().contains(a) 
						&& vector.get(a).contains(entity.getValueForAttribute(a.getAttributeName())))
				.count();
	}
	
	// SETTER
	
	/**
	 * Add new value match to this attribute vector
	 * 
	 * @param matches
	 */
	public void addMatchToVector(IValue... matches){
		vector.putAll(Arrays.asList(matches).stream()
			.collect(Collectors.groupingBy(
					v -> v.getValueSpace().getAttribute(),
					Collectors.toSet())));
	}
	
	//-------------------------------------------//
	
	public Map<IAttribute<? extends IValue>, Set<IValue>> getVector(){
		return Collections.unmodifiableMap(vector);
	}

	public Collection<IAttribute<? extends IValue>> getAttributes() {
		return vector.keySet();
	}

	public Collection<IValue> getValues(IAttribute<? extends IValue> att) {
		return Collections.unmodifiableSet(vector.get(att));
	}

	public Collection<IValue> values() {
		return vector.values().stream().flatMap(s -> s.stream()).collect(Collectors.toSet());
	}
	
}
