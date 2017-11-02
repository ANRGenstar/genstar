package core.metamodel.attribute.demographic;

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
public class OTODemographicAttribute<V extends IValue> extends DemographicAttribute<V> {

	private DemographicAttribute<? extends IValue> referentAttribute;
	private Map<V, ? extends IValue> mapper;

	public OTODemographicAttribute(String name, IValueSpace<V> valueSpace,
			DemographicAttribute<? extends IValue> referentAttribute, Map<V, ? extends IValue> mapper) {
		super(name, valueSpace);
		this.referentAttribute = referentAttribute;
		this.mapper = mapper;
	}
	
	@Override
	public boolean isLinked(DemographicAttribute<? extends IValue> attribute){
		return attribute.equals(referentAttribute);	
	}
	
	@Override
	public DemographicAttribute<? extends IValue> getReferentAttribute(){
		return referentAttribute;
	}
	
	@Override
	public Set<IValue> findMappedAttributeValues(IValue value){
		if(!mapper.containsKey(value) || !mapper.containsValue(value))
			throw new NullPointerException("The value "+value+" is not part of any known linked attribute ("
				+ this + " || "+referentAttribute+ ")");
		return Set.of(mapper.containsKey(value) ? mapper.get(value) : mapper.keySet().stream()
				.filter(key -> mapper.get(key).equals(value)).findAny().get());
	}

}
