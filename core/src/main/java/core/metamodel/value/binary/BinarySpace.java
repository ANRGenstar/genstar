package core.metamodel.value.binary;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import core.metamodel.IAttribute;
import core.metamodel.value.IValueSpace;
import core.util.data.GSEnumDataType;

public class BinarySpace implements IValueSpace<BooleanValue> {
	
	private static Map<IAttribute, BinarySpace> multiton = new HashMap<>();
	
	private Set<BooleanValue> values;
	private IAttribute attribute;
	
	private BinarySpace(IAttribute attribute){
		this.attribute = attribute;
		this.values = Stream.of(new BooleanValue(this, true), new BooleanValue(this, false))
				.collect(Collectors.toSet());
	}
	
	/**
	 * Instances getter as mandatory constructor that complies to multiton
	 * design patterns
	 * 
	 * @param attribute
	 * @return
	 */
	public static BinarySpace getInstance(IAttribute attribute){
		if(multiton.containsKey(attribute))
			return multiton.get(attribute);
		multiton.put(attribute, new BinarySpace(attribute));
		return multiton.get(attribute);
	}
	
	// ---------------------------------------------------------------------- //

	@Override
	public BooleanValue getValue(String value) throws IllegalArgumentException {
		if(!value.equalsIgnoreCase(Boolean.TRUE.toString()) 
				&& !value.equalsIgnoreCase(Boolean.FALSE.toString()))
			throw new IllegalArgumentException("The string value "+value+" does not feet a binary value template");
		return values.stream().filter(val -> val.getStringValue().equalsIgnoreCase(value)).findFirst().get();
	}

	@Override
	public BooleanValue retrieveValue(String value) throws NullPointerException {
		try {
			return getValue(value);
		} catch (IllegalArgumentException e) {
			throw new NullPointerException("The string value "+value
					+" cannot be resolve to boolean as defined by "+this.getClass().getSimpleName());
		}
	}

	@Override
	public IAttribute getAttribute() {
		return attribute;
	}
	
	@Override
	public GSEnumDataType getType() {
		return GSEnumDataType.Boolean;
	}

}
