package io.metamodel.attribut;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import io.metamodel.attribut.value.IValue;
import io.util.data.GSEnumDataType;

/**
 * TODO: javadoc
 * 
 * @author kevinchapuis
 *
 */
public class MappedAttribute extends AbstractAttribute implements IAttribute {

	private Map<Set<String>, Set<String>> mapper;

	public MappedAttribute(String name, GSEnumDataType dataType, IAttribute referentAttribute,
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
	public IValue findMatchingAttributeValue(IValue val) {
		if(mapper.values().stream().anyMatch(set -> set.contains(val.getInputStringValue())))
			return mapper.entrySet().stream().filter(e -> e.getValue()
					.stream().anyMatch(s -> s.equals(val.getInputStringValue())))
					.map(e -> getValues().stream().filter(av -> av.getInputStringValue().equals(e.getKey())).findFirst().get())
					.findFirst().get();
		if(mapper.keySet().stream().anyMatch(set -> set.contains(val.getInputStringValue())))
			return mapper.keySet().stream().filter(s -> s.contains(val.getInputStringValue()))
					.map(s -> getValues().stream().filter(av -> s.contains(av.getInputStringValue())).findFirst().get())
					.findFirst().get();
		return getEmptyValue();
	}
	
}
