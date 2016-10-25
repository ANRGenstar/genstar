package core.io.survey.attribut;

import core.util.data.GSEnumDataType;

public class UniqueAttribute extends ASurveyAttribute {

	public UniqueAttribute(String name, GSEnumDataType dataType) {
		super(name, dataType);
	}

	@Override
	public boolean isRecordAttribute() {
		return false;
	}

}
