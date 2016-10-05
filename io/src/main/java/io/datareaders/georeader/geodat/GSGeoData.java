package io.datareaders.georeader.geodat;

public class GSGeoData implements IGeoData {

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

}
