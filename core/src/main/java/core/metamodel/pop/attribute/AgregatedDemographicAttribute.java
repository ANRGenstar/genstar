package core.metamodel.pop.attribute;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import core.metamodel.value.IValue;
import core.metamodel.value.IValueSpace;

public class AgregatedDemographicAttribute<V extends IValue> extends DemographicAttribute<V> {

	private Map<V, Set<V>> mapper;
	protected DemographicAttribute<V> referent;
	
	public AgregatedDemographicAttribute(String name, IValueSpace<V> valueSpace,
			DemographicAttribute<V> referent, Map<V, Set<V>> mapper) {
		super(name, valueSpace);
		this.referent = referent;
		this.mapper = mapper;
	}

	@Override
	public boolean isLinked(DemographicAttribute<? extends IValue> attribute){
		return attribute.equals(referent);	
	}
	
	@Override
	public DemographicAttribute<V> getReferentAttribute(){
		return referent;
	}

	@Override
	public Set<V> findMappedAttributeValues(IValue value){
		if(mapper.containsKey(value))
			return mapper.get(value);
		if(mapper.values().contains(value))
			return mapper.entrySet().stream().filter(e -> e.getValue().contains(value))
					.map(e -> e.getKey()).collect(Collectors.toSet());
		throw new NullPointerException("The value "+value+" is not part of any known link attribute ("
				+ this + " < "+referent + ")");
	}
	
	
}
