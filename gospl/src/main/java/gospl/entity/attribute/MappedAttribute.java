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
 * TODO: javadoc
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

		// or, is val part of the values that map to a key ?
		Optional<Entry<Set<String>, Set<String>>> optMap2 = mapper.entrySet().stream()
				.filter(e -> e.getValue().contains(val.getInputStringValue())).findFirst();
		if (optMap2.isPresent()) 
			return this.getValues().stream()
					.filter(refVal -> optMap2.get().getKey().contains(refVal.getInputStringValue()))
					.collect(Collectors.toSet());
		
		// is val part of the keys that map to something ?
		Optional<Entry<Set<String>, Set<String>>> optMap = mapper.entrySet().stream() .filter(e -> e.getKey().contains(val.getInputStringValue())).findFirst();
		if(optMap.isPresent())
			return this.getReferentAttribute().getValues().stream()
					.filter(refVal -> optMap.get().getValue().contains(refVal.getInputStringValue()))
					.collect(Collectors.toSet());
		
		// maybe nothing was found; in this case return the empty value
		return Stream.of(this.getEmptyValue()).collect(Collectors.toSet());
	}

}
