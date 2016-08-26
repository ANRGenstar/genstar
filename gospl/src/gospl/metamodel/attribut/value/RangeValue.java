package gospl.metamodel.attribut.value;

import gospl.metamodel.attribut.GosplValueType;
import gospl.metamodel.attribut.IAttribute;
import io.datareaders.DataType;

public class RangeValue implements IValue {

	private DataType dataType;
	private IAttribute attribute;
	
	private String inputStringValue;
	private String inputStringLowerBound;
	private String inputStringUpperBound;

	public RangeValue(String inputStringLowerBound, String inputStringUpperBound, String inputStringValue, DataType dataType, IAttribute attribute) {
		this.inputStringLowerBound = inputStringLowerBound;
		this.inputStringUpperBound = inputStringUpperBound;
		this.inputStringValue = inputStringValue;
		this.dataType = dataType;
		this.attribute = attribute;
	}
	
	public RangeValue(DataType dataType, IAttribute attribute) {
		this(GosplValueType.unique.getDefaultStringValue(dataType), GosplValueType.unique.getDefaultStringValue(dataType), 
				GosplValueType.range.getDefaultStringValue(dataType), dataType, attribute);
	}

	@Override
	public String getInputStringValue() {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		return attribute;
	}

	@Override
	public DataType getDataType() {
		// TODO Auto-generated method stub
		return dataType;
	}

}
