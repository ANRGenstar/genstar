package gospl.metamodel.attribut.value;

import gospl.metamodel.attribut.IAttribute;
import io.data.GSDataType;

public abstract class AValue implements IValue {

	private GSDataType dataType;
	private IAttribute attribute;
	
	private String inputStringValue;
	
	public AValue(String inputStringValue, GSDataType dataType, IAttribute attribute) {
		this.inputStringValue = inputStringValue;
		this.dataType = dataType;
		this.attribute = attribute;
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
	
	@Override
	public String toString(){
		return inputStringValue+" ("+attribute.getName()+")";
	}

}
