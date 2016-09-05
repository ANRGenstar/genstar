package gospl.metamodel.attribut;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import gospl.metamodel.attribut.value.IValue;
import io.data.GSDataType;

public class AggregatedAttribute extends AbstractAttribute implements IAttribute {

	private Map<String, Set<String>> mapper;

	public AggregatedAttribute(String name, GSDataType dataType, IAttribute referentAttribute,
			Map<String, Set<String>> mapper) {
		super(name, dataType, referentAttribute);
		this.mapper = mapper;
	}
	
	@Override
	public boolean isRecordAttribute() {
		return false;
	}

	public Map<String, Set<String>> getMapper(){
		return Collections.unmodifiableMap(mapper);
	}

	@Override
	public IValue findMatchingAttributeValue(IValue val) {
		if(mapper.values().stream().flatMap(set -> set.stream()).anyMatch(s -> s.equals(val.getInputStringValue())))
			return mapper.entrySet().stream().filter(e -> e.getValue()
					.stream().anyMatch(s -> s.equals(val.getInputStringValue())))
					.map(e -> getValues().stream().filter(av -> av.getInputStringValue().equals(e.getKey())).findFirst().get())
					.findFirst().get();
		if(mapper.keySet().stream().anyMatch(s -> s.equals(val.getInputStringValue())))
			return mapper.keySet().stream().filter(s -> s.equals(val.getInputStringValue()))
					.map(s -> getValues().stream().filter(av -> av.getInputStringValue().equals(s)).findFirst().get())
					.findFirst().get();
		return getEmptyValue();
	}
	
}
