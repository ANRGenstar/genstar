package core.metamodel.value.numeric;

import core.metamodel.value.IValue;
import core.util.data.GSEnumDataType;

public class DoubleValue implements IValue {

	private double value;
	private ContinuedSpace vs;

	public DoubleValue(ContinuedSpace vs, double value) {
		this.vs = vs;
		this.value = value;
	}

	@Override
	public GSEnumDataType getType() {
		return GSEnumDataType.Double;
	}

	@Override
	public String getStringValue() {
		return String.valueOf(value);
	}

	@Override
	public ContinuedSpace getValueSpace() {
		return this.vs;
	}
	
	/**
	 * The actual encapsulated value
	 * @return
	 */
	public double getActualValue(){
		return value;
	}

}
