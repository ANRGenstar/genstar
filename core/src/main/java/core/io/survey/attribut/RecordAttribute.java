package core.io.survey.attribut;

import core.metamodel.IValue;
import core.util.data.GSEnumDataType;

public class RecordAttribute extends ASurveyAttribute {

	private IValue recordValue;
	
	public RecordAttribute(String name, GSEnumDataType dataType, ASurveyAttribute referentAttribute) {
		super(name, dataType, referentAttribute);
	}

	@Override
	public boolean isRecordAttribute() {
		return true;
	}

	@Override
	public IValue findMatchingAttributeValue(IValue val) {
		if(val.equals(recordValue))
			return recordValue;
		return null;
	}

}
