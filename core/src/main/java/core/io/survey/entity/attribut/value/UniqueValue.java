package core.io.survey.entity.attribut.value;

import core.io.survey.entity.attribut.ASurveyAttribute;
import core.io.survey.entity.attribut.GSEnumAttributeType;
import core.util.data.GSEnumDataType;

public class UniqueValue extends ASurveyValue {

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
