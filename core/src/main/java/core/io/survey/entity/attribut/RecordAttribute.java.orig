package core.io.survey.entity.attribut;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import core.io.survey.entity.attribut.value.AGenstarValue;
import core.util.data.GSEnumDataType;

public class RecordAttribute extends AGenstarAttribute {
	
	public RecordAttribute(String name, GSEnumDataType dataType, AGenstarAttribute referentAttribute) {
		super(name, dataType, referentAttribute);
	}

	@Override
	public boolean isRecordAttribute() {
		return true;
	}

	@Override
	public Set<AGenstarValue> findMappedAttributeValues(AGenstarValue val) {
		return Stream.of(this.getEmptyValue()).collect(Collectors.toSet());
	}

}
