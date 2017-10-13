package spll.entity.attribute.value;

import core.metamodel.geo.AGeoAttribute;
import core.metamodel.value.geo.IValue;

public class RawGeoData extends IValue {

	private Number numVal;

	public RawGeoData(AGeoAttribute attribute, String valString, Object valNative){
		super(valString, valNative.toString(), attribute);
		this.numVal = null;
	}
	
	public RawGeoData(AGeoAttribute attribute, Object val) {
		this(attribute, val.toString(), val);
	}
	
	public RawGeoData(AGeoAttribute attribute, String  valString, Number val) {
		super(valString, val.toString(), attribute);
		this.numVal = val;
	}
	
	public RawGeoData(AGeoAttribute attribute, Number val) {
		this(attribute, val.toString(), val);
	}

	@Override
	public Number getNumericalValue() {
		return numVal;
	}
	
	@Override
	public boolean isNumericalValue() {
		if(numVal == null)
			return false;
		return true;
	}
	
	@Override
	public String toString(){
		return super.stringVal;
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
		RawGeoData other = (RawGeoData) obj;
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
