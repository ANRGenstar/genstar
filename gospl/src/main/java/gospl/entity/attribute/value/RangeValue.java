package gospl.entity.attribute.value;

import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationValue;
import core.util.data.GSEnumDataType;
import gospl.entity.attribute.GSEnumAttributeType;

public class RangeValue extends APopulationValue {
	
	private String inputStringLowerBound;
	private String inputStringUpperBound;

	public RangeValue(String inputStringLowerBound, String inputStringUpperBound, 
			String inputStringValue, GSEnumDataType dataType, APopulationAttribute attribute) {
		super(inputStringValue, dataType, attribute);
		this.inputStringLowerBound = inputStringLowerBound;
		this.inputStringUpperBound = inputStringUpperBound;
	}
	
	public RangeValue(GSEnumDataType dataType, APopulationAttribute attribute) {
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
