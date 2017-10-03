package core.metamodel.value.numeric;

import java.util.Optional;
import java.util.Set;

import core.metamodel.IAttribute;
import core.metamodel.value.IValueSpace;
import core.util.data.GSDataParser;
import core.util.data.GSEnumDataType;

public class ContinuedSpace implements IValueSpace<DoubleValue> {

	private static GSDataParser gsdp = new GSDataParser();

	private Set<DoubleValue> values;
	private double min, max;
	
	@Override
	public GSEnumDataType getType() {
		return GSEnumDataType.Double;
	}

	@Override
	public DoubleValue getValue(String value) {
		if(!gsdp.getValueType(value).isNumericValue())
			throw new IllegalArgumentException("The string value "+value+" does not feet a discrete "
					+ "integer value template");
		
		double currentVal = gsdp.getDouble(value);
		if(currentVal < min || currentVal > max)
			throw new IllegalArgumentException("Proposed value "+value+" is "
					+ (currentVal < min ? "below" : "beyond") + " given bound ("
							+ (currentVal < min ? min : max) + ")");
		
		DoubleValue iv = null;
		try {
			iv = retrieveValue(value);
		} catch (NullPointerException e) {
			iv = new DoubleValue(this, currentVal);
			values.add(iv);
		}
		return iv;
	}
	
	@Override
	public DoubleValue retrieveValue(String value) throws NullPointerException {
		Optional<DoubleValue> opValue = values.stream()
				.filter(val -> val.getActualValue() == gsdp.getDouble(value)).findAny();
		if(opValue.isPresent())
			return opValue.get();
		throw new NullPointerException("The string value "+value+" is not comprise "
				+ "in the value space "+this.toString());
	}

	@Override
	public IAttribute getAttribute() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString(){
		return this.getAttribute().getAttributeName()+"_"+this.getType();
	}
	
}
