package gospl.metamodel.attribut.value;

import gospl.metamodel.attribut.GosplValueType;
import gospl.metamodel.attribut.IAttribute;
import io.data.GSDataType;

public class RangeValue implements IValue {

	private GSDataType dataType;
	private IAttribute attribute;
	
	private String inputStringValue;
	private String inputStringLowerBound;
	private String inputStringUpperBound;

	public RangeValue(String inputStringLowerBound, String inputStringUpperBound, String inputStringValue, GSDataType dataType, IAttribute attribute) {
		this.inputStringLowerBound = inputStringLowerBound;
		this.inputStringUpperBound = inputStringUpperBound;
		this.inputStringValue = inputStringValue;
		this.dataType = dataType;
		this.attribute = attribute;
	}
	
	public RangeValue(GSDataType dataType, IAttribute attribute) {
		this(GosplValueType.unique.getDefaultStringValue(dataType), GosplValueType.unique.getDefaultStringValue(dataType), 
				GosplValueType.range.getDefaultStringValue(dataType), dataType, attribute);
	}

	@Override
	public String getInputStringValue() {
		return inputStringValue;
	}
	
	public String getInputStringLowerBound(){
		return inputStringLowerBound;
	}
	
	public String getInputStringUpperBound(){
		return inputStringUpperBound;
	}

	@Override
	public IAttribute getAttribute() {
		return attribute;
	}

	@Override
	public GSDataType getDataType() {
		return dataType;
	}

}
