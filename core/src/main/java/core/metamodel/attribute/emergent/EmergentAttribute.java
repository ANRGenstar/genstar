package core.metamodel.attribute.emergent;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import core.configuration.jackson.EmergentAttributeSerializer;
import core.metamodel.attribute.IAttribute;
import core.metamodel.attribute.demographic.DemographicAttribute;
import core.metamodel.attribute.emergent.function.IEntityEmergentFunction;
import core.metamodel.entity.IEntity;
import core.metamodel.value.IValue;

/**
 * Attribute that can retrieve value based on emergent properties. It is based on
 * the implementation of a {@link EmergentAttribute}
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
public class EmergentAttribute<V extends IValue, 
	E extends IEntity<? extends IAttribute<? extends IValue>>,
			U> 
	extends DemographicAttribute<V> {

	public static final String SELF = "EMERGENT ATTRIBUTE";
	public static final String FUNCTION = "EMERGENT FUNCTION";
	
	@JsonProperty(FUNCTION)
	private IEntityEmergentFunction<E, U, V> function;

	protected EmergentAttribute(String name) {
		super(name);
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
	public V getEmergentValue(E entity, U predicate) {
		return function.apply(entity, predicate);
	}
	
	/**
	 * The main function that will look at sub-entities property to asses super entity attribute value 
	 * 
	 * @return
	 */
	@JsonProperty(FUNCTION)
	public IEntityEmergentFunction<E, U, V> getFunction(){
		return this.function;
	}
	
	/**
	 * Defines the main function to make attribute value emerge from sub entities properties
	 * 
	 * @param function
	 */
	@JsonProperty(FUNCTION)
	public void setFunction(IEntityEmergentFunction<E, U, V> function){
		this.function = function;
	}

}
