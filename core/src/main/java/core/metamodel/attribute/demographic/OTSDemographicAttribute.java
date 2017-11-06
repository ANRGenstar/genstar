package core.metamodel.attribute.demographic;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import core.metamodel.attribute.IValueSpace;
import core.metamodel.value.IValue;

/**
 * Attribute links to a referent one: each value of this attribute is bound
 * to at least one referent value. This means that this attribute has fewer
 * values than its referent attribute.
 * <p>
 * OTS means: One To Several
 * <p>
 * Self aggregated value -> referent disaggregated values
 * 
 * @see DemographicAttribute
 * 
 * @author kevinchapuis
 *
 * @param <V>
 */
public class OTSDemographicAttribute<V extends IValue> extends MappedDemographicAttribute<V, V> {

	private Map<V, Set<V>> mapper;
	
	public OTSDemographicAttribute(String name, IValueSpace<V> valueSpace,
			DemographicAttribute<V> referentAttribute, Map<V, Set<V>> mapper) {
		super(name, valueSpace, referentAttribute);
		this.mapper = new HashMap<>(mapper);
	}

	@Override
	public Set<V> findMappedAttributeValues(IValue value){
		if(mapper.containsKey(value))
			return mapper.get(value);
		if(mapper.values().contains(value))
			return mapper.entrySet().stream().filter(e -> e.getValue().contains(value))
					.map(e -> e.getKey()).collect(Collectors.toSet());
		throw new NullPointerException("The value "+value+" is not part of any known link attribute ("
				+ this + " < "+this.getReferentAttribute()+ ")");
	}

	@Override
	public boolean addMappedValue(V mapTo, V mapWith) {
		if(mapper.containsKey(mapTo) ||
				this.getValueSpace().contains(mapTo)
				&& getReferentAttribute().getValueSpace().contains(mapWith))
			return mapper.get(mapTo).add(mapWith);
		return false;
	}
	
	
}
