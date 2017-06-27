package gospl.entity.attribute;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationValue;
import core.util.data.GSEnumDataType;

/**
 * This attribute stands for one that refers to another attribute. The binding
 * should be explicitly made using a {@code mapper} to pass as an argument of
 * the constructor. It must define the relationship between these attribute
 * values (as map key) and the referent attribute values (as map value). 
 * 
 * @author kevinchapuis
 * @author Duc an vo
 *
 */
public class MappedAttribute extends APopulationAttribute {

	/*
	 * Keys refer to this attribute values, and values refer to referent attribute values
	 */
	private Map<Set<String>, Set<String>> mapper;

	/**
	 * WARNING: mapper must comply to map-key being this attribute's value and map-value
	 * being the referent attribute's value
	 * 
	 * @param name
	 * @param dataType
	 * @param referentAttribute
	 * @param mapper
	 */
	public MappedAttribute(String name, GSEnumDataType dataType, APopulationAttribute referentAttribute,
			Map<Set<String>, Set<String>> mapper) {
		super(name, dataType, referentAttribute);
		this.mapper = mapper;
	}

	@Override
	public boolean isRecordAttribute() {
		return false;
	}

	public Map<Set<String>, Set<String>> getMapper(){
		return Collections.unmodifiableMap(mapper);
	}

	@Override
	public Set<APopulationValue> findMappedAttributeValues(APopulationValue val) {

		// is val part of the values that map to a key ?
		Optional<Entry<Set<String>, Set<String>>> optMap2 = mapper.entrySet().stream()
				.filter(e -> e.getValue().contains(val.getInputStringValue())).findFirst();
		// is val part of the keys that map to something ?
		Optional<Entry<Set<String>, Set<String>>> optMap = mapper.entrySet().stream()
				.filter(e -> e.getKey().contains(val.getInputStringValue())).findFirst();
		
		boolean selfValAtt = false, referentValAtt = false;
		if(optMap.isPresent() && optMap2.isPresent()){
			selfValAtt = val.getAttribute().equals(this) ? true : false;
			referentValAtt = val.getAttribute().equals(this.getReferentAttribute()) ? true : false;
		}
		
		if(selfValAtt && referentValAtt)
			throw new RuntimeException("Try to find mapped values but "+val+" pertain to "
					+this.getAttributeName()+" and referent "
					+this.getReferentAttribute().getAttributeName()+" attributes");
		
		if((optMap2.isPresent() && !optMap.isPresent()) || referentValAtt) 
			return this.getValues().stream()
					.filter(refVal -> optMap2.get().getKey().contains(refVal.getInputStringValue()) 
							|| optMap2.get().getKey().contains(refVal.getStringValue()))
					.collect(Collectors.toSet());
		
		if((optMap.isPresent() && !optMap2.isPresent()) || selfValAtt)
			return this.getReferentAttribute().getValues().stream()
					.filter(refVal -> optMap.get().getValue().contains(refVal.getInputStringValue())
							|| optMap.get().getValue().contains(refVal.getStringValue()))
					.collect(Collectors.toSet());
		
		// maybe nothing was found; in this case return the empty value
		return Stream.of(this.getEmptyValue()).collect(Collectors.toSet());
	}

	

}
