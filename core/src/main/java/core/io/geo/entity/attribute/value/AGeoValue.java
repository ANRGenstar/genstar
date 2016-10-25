package core.io.geo.entity.attribute.value;

import core.io.geo.entity.attribute.AGeoAttribute;
import core.metamodel.IValue;
import core.util.data.GSDataParser;
import core.util.data.GSEnumDataType;

/**
 * TODO: move to generic IValue
 * 
 * @author kevinchapuis
 *
 */
public abstract class AGeoValue implements IValue {
	
	protected String stringVal;
	protected String inputStringVal;
	
	private AGeoAttribute attribute;

	public AGeoValue(String stringVal, String inputStringVal, AGeoAttribute attribute){
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
	
	public Number getNumericalValue(){
		GSDataParser gsdp = new GSDataParser();
		GSEnumDataType dataType = gsdp.getValueType(stringVal); 
		if(dataType.equals(GSEnumDataType.Integer) || dataType.equals(GSEnumDataType.Double))
			return gsdp.parseNumber(stringVal);
		return Double.NaN;
	}
	
	public abstract boolean isNumericalValue();
	
}
