package gospl.metamodel.attribut.value;

import gospl.metamodel.attribut.GosplValueType;
import gospl.metamodel.attribut.IAttribute;
import io.datareaders.DataType;

public class RecordValue implements IValue {

	private String inputStringValue;
	private DataType dataType;
	private IAttribute attribute;

	public RecordValue(String inputStringValue, DataType dataType, IAttribute attribute) {
		this.inputStringValue = inputStringValue;
		this.dataType = dataType;
		this.attribute = attribute;
	}
	
	public RecordValue(DataType dataType, IAttribute attribute) {
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
	public DataType getDataType() {
		// TODO Auto-generated method stub
		return dataType;
	}

}
