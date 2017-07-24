package gospl.entity.attribute;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationValue;
import core.util.data.GSEnumDataType;
import gospl.entity.attribute.value.UniqueValue;

/**
 * TODO: javadoc
 * 
 * @author kevinchapuis
 * @author Duc an vo
 */
public class RecordAttribute extends APopulationAttribute {
	
	public RecordAttribute(String name, GSEnumDataType dataType, APopulationAttribute referentAttribute) {
		super(name, dataType, referentAttribute);
	}

	@Override
	public boolean isRecordAttribute() {
		return true;
	}

	@Override
	public Set<APopulationValue> findMappedAttributeValues(APopulationValue val) {
		return Stream.of(this.getEmptyValue()).collect(Collectors.toSet());
	}

	@Override
	public APopulationValue getValue(String name) {
		
		APopulationValue res = getInputString2value().get(name);
		
		if (res == null) {
			// we are record, so we accept to create novel values on the fly 
			res = new UniqueValue(name.trim(), this.dataType, this);
			values.add(res);
			if (inputString2value != null)
				inputString2value.put(name, res);
		}
		
		
		return res;
	}
	

}
