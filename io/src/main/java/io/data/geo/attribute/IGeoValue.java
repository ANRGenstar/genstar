package io.data.geo.attribute;

/**
 * TODO: move to generic IValue
 * 
 * @author kevinchapuis
 *
 */
public interface IGeoValue {

	public String getValue();
	
	public Number getNumericalValue();
	
	public boolean isNumericalData();
	
}
