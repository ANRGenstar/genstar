package gospl.entity.attribute.value;

import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationValue;
import core.util.data.GSEnumDataType;
import gospl.entity.attribute.GSEnumAttributeType;

public class UniqueValue extends APopulationValue {

	public UniqueValue(String inputStringValue, String mappedStringValue, GSEnumDataType dataType, APopulationAttribute attribute){
		super(inputStringValue, mappedStringValue, dataType, attribute);
	}
	
	public UniqueValue(String inputStringValue, GSEnumDataType dataType, APopulationAttribute attribute) {
		super(inputStringValue, dataType, attribute);
	}
	
	public UniqueValue(GSEnumDataType dataType, APopulationAttribute attribute) {
		this(GSEnumAttributeType.unique.getDefaultStringValue(dataType), dataType, attribute);
	}

}
