package core.io.survey.attribut.value;

import core.io.survey.attribut.ASurveyAttribute;
import core.io.survey.attribut.GSEnumAttributeType;
import core.util.data.GSEnumDataType;

public class UniqueValue extends AValue {

	public UniqueValue(String inputStringValue, String mappedStringValue, GSEnumDataType dataType, ASurveyAttribute attribute){
		super(inputStringValue, mappedStringValue, dataType, attribute);
	}
	
	public UniqueValue(String inputStringValue, GSEnumDataType dataType, ASurveyAttribute attribute) {
		super(inputStringValue, dataType, attribute);
	}
	
	public UniqueValue(GSEnumDataType dataType, ASurveyAttribute attribute) {
		this(GSEnumAttributeType.unique.getDefaultStringValue(dataType), dataType, attribute);
	}

}
