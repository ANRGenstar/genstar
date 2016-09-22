package gospl.metamodel.attribut;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import gospl.metamodel.attribut.value.IValue;
import io.data.GSDataType;

/**
 * TODO: move to ConnectedAttribute to connect two attributes either to be aggregated or just partially linked
 * WARNING: change disintegration to always keep the most disintegrated data
 * 
 * @author kevinchapuis
 *
 */
public class MappedAttribute extends AbstractAttribute implements IAttribute {

	private Map<Set<String>, Set<String>> mapper;

	public MappedAttribute(String name, GSDataType dataType, IAttribute referentAttribute,
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
