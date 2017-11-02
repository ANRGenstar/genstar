package core.metamodel.attribute.demographic;

import java.util.Collections;
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
public class STSDemographicAttribute<V extends IValue> extends DemographicAttribute<V> {

	private Map<Set<V>, Set<IValue>> map;
	protected DemographicAttribute<? extends IValue> referent;

	public STSDemographicAttribute(String name, IValueSpace<V> valueSpace,
			DemographicAttribute<? extends IValue> referent, Map<Set<V>, Set<IValue>> map) {
		super(name, valueSpace);
		this.referent = referent;
		this.map = map;
	}
	
	@Override
	public boolean isLinked(DemographicAttribute<? extends IValue> attribute){
		return attribute.equals(referent);	
	}
	
	@Override
	public DemographicAttribute<? extends IValue> getReferentAttribute(){
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
