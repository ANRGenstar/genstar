package gospl.metamodel.attribut.value;

import gospl.metamodel.attribut.GosplValueType;
import gospl.metamodel.attribut.IAttribute;
import io.data.GSDataType;

public class RecordValue implements IValue {

	private String inputStringValue;
	private GSDataType dataType;
	private IAttribute attribute;

	public RecordValue(String inputStringValue, GSDataType dataType, IAttribute attribute) {
		this.inputStringValue = inputStringValue;
		this.dataType = dataType;
		this.attribute = attribute;
	}
	
	public RecordValue(GSDataType dataType, IAttribute attribute) {
		this(GosplValueType.record.getDefaultStringValue(dataType), dataType, attribute);
	}

	@Override
	public String getInputStringValue() {
		// TODO Auto-generated method stub
		return inputStringValue;
	}

	@Override
	public IAttribute getAttribute() {
		// TODO Auto-generated method stub
		return attribute;
	}

	@Override
	public GSDataType getDataType() {
		// TODO Auto-generated method stub
		return dataType;
	}

}
