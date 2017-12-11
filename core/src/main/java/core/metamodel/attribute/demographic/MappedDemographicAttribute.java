package core.metamodel.attribute.demographic;

import java.util.Arrays;
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import core.metamodel.attribute.demographic.map.IAttributeMapper;
import core.metamodel.value.IValue;

/**
 * Mapped attribute contains:
 * <p>
 * 1 - must point to a referent attribute <br>
 * 2 - a mapping between self and referent values
 * <p>
 * The mapping is delegated to {@link IAttributeMapper}
 * 
 * @author kevinchapuis
 *
 * @param <K> value type associated with this attribute
 * @param <V> value type associated with referent attribute
 */
@JsonTypeName(MappedDemographicAttribute.SELF)
public class MappedDemographicAttribute<K extends IValue, V extends IValue> extends DemographicAttribute<K> {
	
	public static final String SELF = "MAPPED DEMOGRAPHIC ATTRIBUTE";
	
	public static final String REF = "REFERENT ATTRIBUTE";
	public static final String MAP = "MAPPER";
	
	@JsonProperty(MappedDemographicAttribute.REF)
	private DemographicAttribute<V> referentAttribute;
	
	private IAttributeMapper<K, V> attributeMapper;

	protected MappedDemographicAttribute(String name, DemographicAttribute<V> referentAttribute,
			IAttributeMapper<K, V> attributeMapper) {
		super(name);
		this.referentAttribute = referentAttribute;
		this.attributeMapper = attributeMapper;
	}
	
	// ------------------------------------------------------------------ //
	
	@Override
	public boolean isLinked(DemographicAttribute<? extends IValue> attribute){
		return attribute.equals(referentAttribute);	
	}
	
	@Override
	public DemographicAttribute<V> getReferentAttribute(){
		return referentAttribute;
	}
	
	@Override
	public Collection<? extends IValue> findMappedAttributeValues(IValue value){
		try {
			return attributeMapper.getMappedValues(value);
		} catch (NullPointerException e) {
			if(getReferentAttribute().getValueSpace().contains(value))
				return Arrays.asList(this.getEmptyValue());
			if(this.getValueSpace().contains(value))
				return Arrays.asList(this.getReferentAttribute().getEmptyValue());
			if(this.getEmptyValue().equals(value) || 
					getReferentAttribute().getValueSpace().getEmptyValue().equals(value))
				return Arrays.asList(value);
			throw e;
		}
	}
	
	// ------------------------------------------------------------------- //
	
	@JsonProperty(MappedDemographicAttribute.MAP)
	public IAttributeMapper<K, V> getAttributeMapper(){
		return attributeMapper;
	}
	
	/**
	 * Add a pair of mapped key / value. Depending on inner {@link IAttributeMapper} implementation
	 * could lead to: 1) Add a new pair 2) Add a value to existing pairwised key / set of value in
	 * aggregated mapper 3) add a key or a value to an existing pair in undirected mapper
	 * 
	 * @param mapTo
	 * @param mapWith
	 * @return
	 */
	public boolean addMappedValue(K mapTo, V mapWith) {
		return this.attributeMapper.add(mapTo, mapWith);
	}
	
	// ------------------------------------------------------------------- //
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((attributeMapper == null) ? 0 : attributeMapper.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		if(!this.isEqual(obj))
			return false;
		@SuppressWarnings("rawtypes")
		MappedDemographicAttribute other = (MappedDemographicAttribute) obj;
		if (attributeMapper == null) {
			if (other.attributeMapper != null)
				return false;
		} else if (!attributeMapper.equals(other.attributeMapper))
			return false;
		return true;
	}

}
