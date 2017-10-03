package core.metamodel.value.binary;

import core.metamodel.value.IValue;
import core.util.data.GSEnumDataType;

public class BooleanValue implements IValue {

	private boolean value;
	private BinarySpace binary;
	
	protected BooleanValue(BinarySpace binary, boolean value){
		this.binary = binary;
		this.value = value;
	}

	@Override
	public GSEnumDataType getType() {
		return GSEnumDataType.Boolean;
	}

	@Override
	public String getStringValue() {
		return String.valueOf(value);
	}

	@Override
	public BinarySpace getValueSpace() {
		return binary;
	}
	
	/**
	 * The actual encapsulated value
	 * @return
	 */
	public boolean getActualValue(){
		return value;
	}

}
