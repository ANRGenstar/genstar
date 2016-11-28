package core.io.survey.attribut;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import core.io.survey.attribut.value.AValue;
import core.util.data.GSEnumDataType;

public class RecordAttribute extends ASurveyAttribute {
	
	public RecordAttribute(String name, GSEnumDataType dataType, ASurveyAttribute referentAttribute) {
		super(name, dataType, referentAttribute);
	}

	@Override
	public boolean isRecordAttribute() {
		return true;
	}

	@Override
	public Set<AValue> findMappedAttributeValues(AValue val) {
		return Stream.of(this.getEmptyValue()).collect(Collectors.toSet());
	}

}
