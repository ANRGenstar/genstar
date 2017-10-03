package core.metamodel.value.numeric;

import core.metamodel.IAttribute;
import core.metamodel.value.IValueSpace;
import core.util.data.GSDataParser;
import core.util.data.GSEnumDataType;
import core.util.data.RangeTemplate;

public class RangeSpace implements IValueSpace<RangeValue> {

	private static GSDataParser gsdp = new GSDataParser();
	private RangeTemplate rt;
	
	private IAttribute attribute;
	
	private int min, max;
	
	public RangeSpace(IAttribute attribute, RangeTemplate rt){
		this(attribute, rt, Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	public RangeSpace(IAttribute attribute, RangeTemplate rt, 
			int minValue, int maxValue) {
		this.attribute = attribute;
		this.rt = rt;
		this.min = minValue;
		this.max = maxValue;
	}

	@Override
	public RangeValue getValue(String value) throws IllegalArgumentException {
		if(!gsdp.getValueType(value).isNumericValue())
			throw new IllegalArgumentException("The string value "+value+" does not feet a discrete "
					+ "integer value template");
		
		int currentVal = gsdp.parseNumber(value).intValue();
		if(currentVal < min || currentVal > max)
			throw new IllegalArgumentException("Proposed value "+value+" is "
					+ (currentVal < min ? "below" : "beyond") + " given bound ("
							+ (currentVal < min ? min : max) + ")");
		
		IntegerValue iv = null;
		try {
			iv = retrieveValue(value);
		} catch (NullPointerException e) {
			iv = new IntegerValue(this, currentVal);
			values.add(iv);
		}
		return iv;
	}

	@Override
	public RangeValue retrieveValue(String value) throws NullPointerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GSEnumDataType getType() {
		return GSEnumDataType.Integer;
	}

	@Override
	public IAttribute getAttribute() {
		return this.attribute;
	}

}
