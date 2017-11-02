package core.metamodel.attribute.demographic;

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
public class OTSDemographicAttribute<V extends IValue> extends DemographicAttribute<V> {

	private Map<V, Set<V>> mapper;
	protected DemographicAttribute<V> referent;
	
	public OTSDemographicAttribute(String name, IValueSpace<V> valueSpace,
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
