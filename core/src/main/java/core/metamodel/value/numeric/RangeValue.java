package core.metamodel.value.numeric;

import core.metamodel.value.IValue;
import core.util.data.GSEnumDataType;

public class RangeValue implements IValue {

	public enum RangeBound{LOWER,UPPER}
	
	private Number lowerbound, upperbound;
	private RangeSpace rs; 
	
	protected RangeValue(RangeSpace rs, Number bound, RangeBound rb){
		this.rs = rs;
		switch (rb) {
		case LOWER: this.lowerbound = bound; this.upperbound = null;
			break;
		case UPPER: this.upperbound = bound; this.lowerbound = null;
		default:
			throw new IllegalArgumentException();
		}
	}
	
	protected RangeValue(RangeSpace rs, Number lowerBound, Number upperbound){
		this.lowerbound = lowerBound;
		this.upperbound = upperbound;
		this.rs = rs;
	}
	
	@Override
	public GSEnumDataType getType() {
		return GSEnumDataType.Integer;
	}

	@Override
	public String getStringValue() {
		if(lowerbound != null && upperbound != null)
			return rs.getRangeTemplate().getMiddleTemplate(lowerbound, upperbound);
		if(lowerbound == null)
			return rs.getRangeTemplate().getUpperTemplate(upperbound);
		return rs.getRangeTemplate().getLowerTemplate(lowerbound);
	}
	
	@Override
	public RangeSpace getValueSpace() {
		return rs;
	}

	/**
	 * The actual encapsulated value
	 * @return
	 */
	public Number[] getActualValue(){
		return new Number[]{lowerbound, upperbound};
	}
	
}
