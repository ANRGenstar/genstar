package core.metamodel.pop;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import core.metamodel.value.IValue;
import core.metamodel.value.IValueSpace;

public class MappedDemographicAttribute<R extends IValue, V extends IValue> extends DemographicAttribute<V> {

	private Map<Set<R>, Set<V>> map;
	protected DemographicAttribute<R> referent;

	public MappedDemographicAttribute(String name, IValueSpace<V> valueSpace,
			DemographicAttribute<R> referent, Map<Set<R>, Set<V>> map) {
		super(name, valueSpace);
		this.referent = referent;
		this.map = map;
	}
	
	@Override
	public boolean isLinked(DemographicAttribute<? extends IValue> attribute){
		return attribute.equals(referent);	
	}
	
	@Override
	public DemographicAttribute<R> getReferentAttribute(){
		return referent;
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
	
}
