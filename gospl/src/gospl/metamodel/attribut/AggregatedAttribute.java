package gospl.metamodel.attribut;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

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
	
}
