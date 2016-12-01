package core.io.survey.entity.attribut.value;

import core.io.survey.entity.attribut.ASurveyAttribute;
import core.io.survey.entity.attribut.GSEnumAttributeType;
import core.util.data.GSEnumDataType;

public class RangeValue extends ASurveyValue {
	
	private String inputStringLowerBound;
	private String inputStringUpperBound;

	public RangeValue(String inputStringLowerBound, String inputStringUpperBound, 
			String inputStringValue, GSEnumDataType dataType, ASurveyAttribute attribute) {
		super(inputStringValue, dataType, attribute);
		this.inputStringLowerBound = inputStringLowerBound;
		this.inputStringUpperBound = inputStringUpperBound;
	}
	
	public RangeValue(GSEnumDataType dataType, ASurveyAttribute attribute) {
		this(GSEnumAttributeType.unique.getDefaultStringValue(dataType), GSEnumAttributeType.unique.getDefaultStringValue(dataType), 
				GSEnumAttributeType.range.getDefaultStringValue(dataType), dataType, attribute);
	}
	
	public String getInputStringLowerBound(){
		return inputStringLowerBound;
	}
	
	public String getInputStringUpperBound(){
		return inputStringUpperBound;
	}

}
