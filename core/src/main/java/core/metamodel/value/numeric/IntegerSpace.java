package core.metamodel.value.numeric;

import java.util.Optional;
import java.util.Set;

import core.metamodel.IAttribute;
import core.metamodel.value.IValueSpace;
import core.util.data.GSDataParser;
import core.util.data.GSEnumDataType;

public class IntegerSpace implements IValueSpace<IntegerValue> {

	private static GSDataParser gsdp = new GSDataParser();
	
	private Set<IntegerValue> values;
	private int min, max;

	private IAttribute attribute;
	
	public IntegerSpace(IAttribute attribute){
		this(attribute, Integer.MIN_VALUE, Integer.MAX_VALUE);
	}
	
	public IntegerSpace(IAttribute attribute, Integer min, Integer max) {
		this.attribute = attribute;
		this.min = min;
		this.max = max;
	}
	
	@Override
	public IntegerValue getValue(String value) {
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
	public IntegerValue retrieveValue(String value) throws NullPointerException {
		Optional<IntegerValue> opValue = values.stream()
				.filter(val -> val.getActualValue() == gsdp.parseNumber(value).intValue())
				.findAny();
		if(opValue.isPresent())
			return opValue.get();
		throw new NullPointerException("The string value "+value+" is not comprise "
				+ "in the value space "+this.toString());
	}


	@Override
	public GSEnumDataType getType() {
		return GSEnumDataType.Integer;
	}

	@Override
	public IAttribute getAttribute() {
		return attribute;
	}
	
	@Override
	public String toString(){
		return this.getAttribute().getAttributeName()+"_"+getType();
	}
	
}
