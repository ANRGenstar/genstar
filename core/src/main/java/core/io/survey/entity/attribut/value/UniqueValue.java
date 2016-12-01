package core.io.survey.entity.attribut.value;

import core.io.survey.entity.attribut.AGenstarAttribute;
import core.io.survey.entity.attribut.GSEnumAttributeType;
import core.util.data.GSEnumDataType;

public class UniqueValue extends AGenstarValue {

	public UniqueValue(String inputStringValue, String mappedStringValue, GSEnumDataType dataType, AGenstarAttribute attribute){
		super(inputStringValue, mappedStringValue, dataType, attribute);
	}
	
	public UniqueValue(String inputStringValue, GSEnumDataType dataType, AGenstarAttribute attribute) {
		super(inputStringValue, dataType, attribute);
	}
	
	public UniqueValue(GSEnumDataType dataType, AGenstarAttribute attribute) {
		this(GSEnumAttributeType.unique.getDefaultStringValue(dataType), dataType, attribute);
	}

}
