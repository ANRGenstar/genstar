package core.metamodel.value.numeric;

import core.metamodel.value.IValue;
import core.util.data.GSEnumDataType;

public class ContinuedValue implements IValue, Comparable<ContinuedValue> {

	private Double value;
	private ContinuedSpace cs;
		
	public ContinuedValue(ContinuedSpace cs, double value) {
		this.cs = cs;
		this.value = value;
	}

	@Override
	public GSEnumDataType getType() {
		return GSEnumDataType.Continue;
	}

	@Override
	public String getStringValue() {
		return String.valueOf(value);
	}
	
	@Override
	public int compareTo(ContinuedValue o) {
		return this.value.compareTo(o.getActualValue());
	}
	
	/**
	 * The actual encapsulated value
	 * @return
	 */
	public double getActualValue(){
		return value;
	}

	@Override
	public ContinuedSpace getValueSpace() {
		return cs;
	}

}
