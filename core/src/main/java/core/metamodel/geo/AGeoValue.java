package core.metamodel.geo;

import core.metamodel.value.IValue;
import core.util.data.GSDataParser;

/**
 * TODO: javadoc
 * 
 * @author kevinchapuis
 *
 */
public abstract class AGeoValue implements IValue {
	
	protected String stringVal;
	protected String inputStringVal;
	
	private AGeoAttribute attribute;

	public AGeoValue(String stringVal, String inputStringVal, AGeoAttribute attribute){
		if(stringVal == null || inputStringVal == null)
			throw new IllegalArgumentException("Cannot instanciate a geo-value with a null value");
		if(attribute == null)
			throw new IllegalArgumentException("Cannot instanciate a geo-value with a null attribute"); 
		this.stringVal = stringVal;
		this.inputStringVal = inputStringVal;
		this.attribute = attribute;
	}
	
	@Override
	public String getStringValue() {
		return stringVal;
	}
	
	@Override
	public String getInputStringValue() {
		return inputStringVal;
	}
	
	@Override
	public AGeoAttribute getAttribute(){
		return attribute;
	}
	
	/**
	 * Retrieve the inner value as a Numerical one. If the value is not inherently numerical
	 * returns Double.NaN
	 * 
	 * @return the value as a {@link Number} type value
	 */
	public Number getNumericalValue(){
		GSDataParser gsdp = new GSDataParser(); 
		if(gsdp.getValueType(stringVal).isNumericValue())
			return gsdp.parseNumber(stringVal);
		return Double.NaN;
	}
	
	/**
	 * Compare this string value with this variable value. Can proceed to a numerical comparison or
	 * a string based comparison depend on value and variable type. In the second case, make use of
	 * {@link String#equalsIgnoreCase(String)} method
	 * 
	 * @param value
	 * @return
	 */
	public boolean valueEquals(String value) {
		if(new GSDataParser().getValueType(value).isNumericValue() && this.isNumericalValue())
			return Double.valueOf(value) == getNumericalValue().doubleValue();
		return value.equalsIgnoreCase(inputStringVal);
	}
	
	public abstract boolean isNumericalValue();	
}
