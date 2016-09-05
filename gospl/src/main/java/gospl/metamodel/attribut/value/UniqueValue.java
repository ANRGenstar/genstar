package gospl.metamodel.attribut.value;

import gospl.metamodel.attribut.GosplValueType;
import gospl.metamodel.attribut.IAttribute;
import io.data.GSDataType;

public class UniqueValue implements IValue {

	private GSDataType dataType;
	private IAttribute attribute;
	private String inputStringValue;

	public UniqueValue(String inputStringValue, GSDataType dataType, IAttribute attribute) {
		this.inputStringValue = inputStringValue;
		this.dataType = dataType;
		this.attribute = attribute;
	}
	
	public UniqueValue(GSDataType dataType, IAttribute attribute) {
		this(GosplValueType.unique.getDefaultStringValue(dataType), dataType, attribute);
	}

	@Override
	public String getInputStringValue() {
		return inputStringValue;
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
