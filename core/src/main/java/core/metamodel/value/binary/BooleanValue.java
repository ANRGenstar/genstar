package core.metamodel.value.binary;

import core.metamodel.value.IValue;
import core.metamodel.value.IValueSpace;
import core.util.data.GSEnumDataType;

public class BooleanValue implements IValue {

	private Boolean value;
	private BinarySpace bs;
		
	protected BooleanValue(BinarySpace bs, Boolean value){
		this.bs = bs;
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
	
	/**
	 * The actual encapsulated value
	 * @return
	 */
	public boolean getActualValue(){
		return value;
	}

	@Override
	public IValueSpace<BooleanValue> getValueSpace() {
		return bs;
	}

}
