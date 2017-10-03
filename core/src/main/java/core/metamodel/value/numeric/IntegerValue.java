package core.metamodel.value.numeric;

import core.metamodel.value.IValue;
import core.util.data.GSEnumDataType;

public class IntegerValue implements IValue {

	private int value;
	private IntegerSpace is;

	protected IntegerValue(IntegerSpace is, int value){
		this.is = is;
		this.value = value;
	}
	
	@Override
	public GSEnumDataType getType() {
		return GSEnumDataType.Integer;
	}

	@Override
	public String getStringValue() {
		return String.valueOf(value);
	}

	@Override
	public IntegerSpace getValueSpace() {
		return this.is;
	}

	/**
	 * The actual encapsulated value
	 * @return
	 */
	public int getActualValue(){
		return value;
	}
	
}
