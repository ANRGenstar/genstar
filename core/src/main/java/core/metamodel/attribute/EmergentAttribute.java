package core.metamodel.attribute;

import java.util.Collection;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import core.configuration.jackson.EmergentAttributeSerializer;
import core.metamodel.attribute.emergent.IEntityEmergentFunction;
import core.metamodel.attribute.mapper.IAttributeMapper;
import core.metamodel.entity.IEntity;
import core.metamodel.value.IValue;

/**
 * Attribute that can retrieve value based on emergent properties.
 * 
 * @see EmergentAttribute
 * 
 * @author kevinchapuis
 *
 * @param <V>
 * @param <E>
 * @param <U>
 */
@JsonTypeName(EmergentAttribute.SELF)
@JsonSerialize(using = EmergentAttributeSerializer.class)
public class EmergentAttribute<K extends IValue, V extends IValue, 
	E extends IEntity<? extends IAttribute<? extends IValue>>,
			U> 
	extends MappedAttribute<K, V> {

	public static final String SELF = "EMERGENT ATTRIBUTE";
	public static final String FUNCTION = "EMERGENT FUNCTION";
	
	@JsonProperty(FUNCTION)
	private IEntityEmergentFunction<E, U, K> function;

	protected EmergentAttribute(String name, Attribute<V> referent, IAttributeMapper<K,V> mapper) {
		super(name, referent, mapper);
	}
	
	protected EmergentAttribute(String name) {
		this(name, null, null);
	}
	
	/**
	 * The main method that can retrieve the value of an attribute based on
	 * any child properties
	 * 
	 * @param entity
	 * @param predicate
	 * @return
	 */
	@JsonIgnore
	public K getEmergentValue(E entity, U predicate) {
		return function.apply(entity, predicate);
	}
	
	/**
	 * The method to translate value attribute into child properties, i.e.
	 * a collection of values from sub entities that characterize emergent attribute value.
	 * Each set of the collection is a potential sub entity
	 * @return
	 */
	@JsonIgnore
	public Collection<Set<IValue>> getImergentValues(K value, U predicate){
		return function.reverse(value, predicate);
	}
	
	/**
	 * The main function that will look at sub-entities property to asses super entity attribute value 
	 * 
	 * @return
	 */
	@JsonProperty(FUNCTION)
	public IEntityEmergentFunction<E, U, K> getFunction(){
		return this.function;
	}
	
	/**
	 * Defines the main function to make attribute value emerge from sub entities properties
	 * 
	 * @param function
	 */
	@JsonProperty(FUNCTION)
	public void setFunction(IEntityEmergentFunction<E, U, K> function){
		this.function = function;
	}

}
