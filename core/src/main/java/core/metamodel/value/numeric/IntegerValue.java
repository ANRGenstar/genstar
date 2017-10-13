package core.metamodel.value.numeric;

import core.metamodel.value.IValue;
import core.util.data.GSEnumDataType;

public class IntegerValue implements IValue {

	private Integer value;
	private IntegerSpace is;
	
	protected IntegerValue(IntegerSpace is){
		this.is = is;
		this.value = null;
	}
	
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

	/**
	 * The actual encapsulated value
	 * @return
	 */
	public int getActualValue(){
		return value;
	}

	@Override
	public IntegerSpace getValueSpace() {
		return is;
	}
	
}
