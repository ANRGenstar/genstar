package core.io.survey.entity.attribut;

import core.util.data.GSEnumDataType;

public class RangeAttribute extends ASurveyAttribute {

	public RangeAttribute(String name, GSEnumDataType dataType) {
		super(name, dataType);
	}

	@Override
	public boolean isRecordAttribute() {
		return false;
	}

}
