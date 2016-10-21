package io.metamodel.attribut.value;

import io.metamodel.attribut.GSEnumAttributeType;
import io.metamodel.attribut.IAttribute;
import io.util.data.GSEnumDataType;

public class UniqueValue extends AValue {

	public UniqueValue(String inputStringValue, String mappedStringValue, GSEnumDataType dataType, IAttribute attribute){
		super(inputStringValue, mappedStringValue, dataType, attribute);
	}
	
	public UniqueValue(String inputStringValue, GSEnumDataType dataType, IAttribute attribute) {
		super(inputStringValue, dataType, attribute);
	}
	
	public UniqueValue(GSEnumDataType dataType, IAttribute attribute) {
		this(GSEnumAttributeType.unique.getDefaultStringValue(dataType), dataType, attribute);
	}

}
