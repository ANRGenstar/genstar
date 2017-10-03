package core.metamodel.value.numeric;

import core.metamodel.value.IValue;
import core.util.data.GSEnumDataType;
import core.util.data.RangeTemplate;

public class RangeValue implements IValue {

	private RangeSpace ds;
	private int lowerbound, upperbound;
	private RangeTemplate rt; 

	@Override
	public GSEnumDataType getType() {
		return GSEnumDataType.Integer;
	}

	@Override
	public String getStringValue() {
		return rt.getStringTemplate(Integer.valueOf(lowerbound), Integer.valueOf(upperbound));
	}

	@Override
	public RangeSpace getValueSpace() {
		return this.ds;
	}

	/**
	 * The actual encapsulated value
	 * @return
	 */
	public int[] getActualValue(){
		return new int[]{lowerbound, upperbound};
	}
	
}
