package core.configuration.jackson.utilclass;

public class RangeVal implements IVal {

	private RangeAtt attribute;
	
	private Number low, high;

	public RangeVal(RangeAtt attribute, Number low, Number high) {
		this.attribute = attribute;
		this.low = low;
		this.high = high;
	}

	@Override
	public IAtt<RangeVal> getAttribute() {return attribute;}

	@Override
	public String getStringValue() {
		return low.toString()
				.concat(attribute.getSeparator())
				.concat(high.toString());
	}
	
	public Number getLow() {return low;}
	
	public Number getHigh() {return high;}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((high == null) ? 0 : high.hashCode());
		result = prime * result + ((low == null) ? 0 : low.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RangeVal other = (RangeVal) obj;
		if (high == null) {
			if (other.high != null)
				return false;
		} else if (!high.equals(other.high))
			return false;
		if (low == null) {
			if (other.low != null)
				return false;
		} else if (!low.equals(other.low))
			return false;
		return true;
	}

}
