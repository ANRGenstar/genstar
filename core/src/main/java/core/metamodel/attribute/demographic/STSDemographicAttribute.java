package core.metamodel.attribute.demographic;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import core.metamodel.attribute.IValueSpace;
import core.metamodel.value.IValue;

/**
 * Complex attribute that links values with values of a referent attribute.
 * <p>
 * STS means: Several To Several
 * 
 * @author kevinchapuis
 *
 * @param <V>
 */
public class STSDemographicAttribute<V extends IValue, M extends IValue> extends MappedDemographicAttribute<V, M> {

	private Map<Set<V>, Set<M>> map;

	public STSDemographicAttribute(String name, IValueSpace<V> valueSpace,
			DemographicAttribute<M> referentAttribute, Map<Set<V>, Set<M>> map) {
		super(name, valueSpace, referentAttribute);
		this.map = new HashMap<>(map);
	}
	
	@Override
	public Set<IValue> findMappedAttributeValues(IValue value){
		if(!map.values().contains(value) || map.keySet().contains(value))
			throw new NullPointerException("Value "+value+" is not linked to this mapped attribute ("+this+")");
		return map.keySet().contains(value) ? 
				Collections.unmodifiableSet(map.get(value))
				: map.entrySet().stream().filter(e -> e.getValue().contains(value))
					.flatMap(e -> e.getKey().stream()).collect(Collectors.toSet());
	}

	@Override
	public boolean addMappedValue(V mapTo, M mapWith) {
		if(map.keySet().stream().anyMatch(set -> set.contains(mapTo))) {
			return map.get(map.keySet().stream().filter(key -> key.contains(mapTo))
					.findFirst().get()).add(mapWith);
		} else if(map.containsValue(mapWith)) {
			return map.keySet().stream().filter(key -> map.get(key).contains(mapWith))
					.findFirst().get().add(mapTo);
		} else if(this.getValueSpace().contains(mapTo) 
				&& this.getReferentAttribute().getValueSpace().contains(mapWith)) {
			map.put(Set.of(mapTo), Set.of(mapWith));
			return true;
		}
		return false;
	}
	
}
