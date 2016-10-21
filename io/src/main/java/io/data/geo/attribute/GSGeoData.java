package io.data.geo.attribute;

public class GSGeoData implements IGeoValue {

	private String stringVal;
	private Number numVal;

	public GSGeoData(Object val) {
		this.stringVal = val.toString();
		this.numVal = null;
	}
	
	public GSGeoData(Number val) {
		this.stringVal = val.toString();
		this.numVal = val;
	}

	@Override
	public String getValue() {
		return stringVal;
	}

	@Override
	public Number getNumericalValue() {
		return numVal;
	}
	
	@Override
	public boolean isNumericalData() {
		if(numVal == null)
			return false;
		return true;
	}
	
	@Override
	public String toString(){
		return stringVal;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((numVal == null) ? 0 : numVal.hashCode());
		result = prime * result + ((stringVal == null) ? 0 : stringVal.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GSGeoData other = (GSGeoData) obj;
		if (numVal == null) {
			if (other.numVal != null)
				return false;
		} else if (!numVal.equals(other.numVal))
			return false;
		if (stringVal == null) {
			if (other.stringVal != null)
				return false;
		} else if (!stringVal.equals(other.stringVal))
			return false;
		return true;
	}

}
