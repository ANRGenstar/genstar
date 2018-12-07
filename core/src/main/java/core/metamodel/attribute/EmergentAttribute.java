package core.metamodel.attribute;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import core.metamodel.attribute.emergent.IGSValueFunction;
import core.metamodel.attribute.emergent.filter.IGSEntitySelector;
import core.metamodel.entity.IEntity;
import core.metamodel.value.IValue;

/**
 * Attribute that can retrieve value based on emergent properties.
 * 
 * @see EmergentAttribute
 * 
 * @author kevinchapuis
 *
 * @param <K> the value type of the attribute
 * @param <V> the value type of the referent attribute
 * @param <E> the type of super entity this attribute will work with
 * @param <U> the type of predicate this attributes needs to make values emerge
 * 
 */
@JsonTypeName(EmergentAttribute.SELF)
//@JsonSerialize(using = EmergentAttributeSerializer.class)
public class EmergentAttribute<V extends IValue, U, F> 
	extends Attribute<V> {

	public static final String SELF = "EMERGENT ATTRIBUTE";
	public static final String FUNCTION = "EMERGENT FUNCTION";
	public static final String TRANSPOSER = "EMERGENT FILTER";
	
	@JsonProperty(FUNCTION)
	private IGSValueFunction<U, V> function;
	@JsonProperty(TRANSPOSER)
	private IGSEntitySelector<U, F> transposer;

	protected EmergentAttribute(String name) {
		super(name);
	}
	
	/**
	 * The main method that can retrieve the value of an attribute based on
	 * any child properties
	 * 
	 * @param entity
	 * @param transposer
	 * @return
	 */
	@JsonIgnore
	public V getEmergentValue(IEntity<? extends IAttribute<? extends IValue>> entity) {
		return function.apply(this.transposer.apply(entity));
	}
		
	/**
	 * The main function that will look at sub-entities property to asses super entity attribute value 
	 * 
	 * @return
	 */
	@JsonProperty(FUNCTION)
	public IGSValueFunction<U, V> getFunction(){
		return this.function;
	}
	
	/**
	 * Defines the main function to make attribute value emerge from sub entities properties
	 * 
	 * @param function
	 */
	@JsonProperty(FUNCTION)
	public void setFunction(IGSValueFunction<U, V> function){
		this.function = function;
	}
	
	/**
	 * The function that will transpose the Entity linked to this attribute to 
	 * any U predicate that will be transpose to value V
	 * 
	 * @return
	 */
	@JsonProperty(TRANSPOSER)
	public IGSEntitySelector<U, F> getTransposer(){
		return this.transposer;
	}
	
	/**
	 * The new transposer to be set
	 * 
	 * @param transposer
	 */
	@JsonProperty(TRANSPOSER)
	public void setTransposer(IGSEntitySelector<U, F> transposer) {
		this.transposer = transposer;
	}

}
