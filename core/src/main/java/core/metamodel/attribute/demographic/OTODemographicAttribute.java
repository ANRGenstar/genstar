package core.metamodel.attribute.demographic;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import core.metamodel.attribute.IValueSpace;
import core.metamodel.value.IValue;

/**
 * Simple attribute that binds each value with value of a referent attribute
 * <p>
 * OTO means: One To One
 * 
 * @author kevinchapuis
 *
 * @param <V>
 */
public class OTODemographicAttribute<V extends IValue, M extends IValue> extends MappedDemographicAttribute<V, M> {

	private Map<V, M> mapper;

	public OTODemographicAttribute(String name, IValueSpace<V> valueSpace,
			DemographicAttribute<M> referentAttribute) {
		super(name, valueSpace, referentAttribute);
		this.mapper = new HashMap<>();
	}
	
	public OTODemographicAttribute(String name, IValueSpace<V> valueSpace,
			DemographicAttribute<M> referentAttribute, Map<V, M> mapper) {
		super(name, valueSpace, referentAttribute);
		this.mapper = new HashMap<>(mapper);
	}
	
	@Override
	public Set<IValue> findMappedAttributeValues(IValue value){
		if(!mapper.containsKey(value) || !mapper.containsValue(value))
			throw new NullPointerException("The value "+value+" is not part of any known linked attribute ("
				+ this + " || "+getReferentAttribute()+ ")");
		return Set.of(mapper.containsKey(value) ? mapper.get(value) : mapper.keySet().stream()
				.filter(key -> mapper.get(key).equals(value)).findAny().get());
	}
	
	/**
	 * Add a pair of self value to referent value
	 * 
	 * @param mapTo
	 * @param mapWith
	 */
	public boolean addMappedValue(V mapTo, M mapWith) {
		if(!this.getValueSpace().contains(mapTo) 
				|| !this.getReferentAttribute().getValueSpace().contains(mapWith))
			return false;
		mapper.put(mapTo, mapWith);
		return true;
	}

}
