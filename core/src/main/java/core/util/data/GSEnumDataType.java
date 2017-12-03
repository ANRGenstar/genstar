package core.util.data;

import java.util.Arrays;
import java.util.Optional;

import core.metamodel.value.IValue;
import core.metamodel.value.binary.BooleanValue;
import core.metamodel.value.categoric.NominalValue;
import core.metamodel.value.categoric.OrderedValue;
import core.metamodel.value.numeric.ContinuousValue;
import core.metamodel.value.numeric.IntegerValue;
import core.metamodel.value.numeric.RangeValue;

/**
 * 
 * 
 * @author kevinchapuis
 * @author Vo Duc An
 *
 */
public enum GSEnumDataType {

	Continue (Double.class, ContinuousValue.class),
	Integer (Integer.class, IntegerValue.class),
	Range (Number.class, RangeValue.class),
	Boolean (Boolean.class, BooleanValue.class),
	Order (String.class, OrderedValue.class),
	Nominal (String.class, NominalValue.class);

	private Class<? extends IValue> wrapperClass;
	private Class<?> concretClass;

	private GSEnumDataType(Class<?> concretClass,
			Class<? extends IValue> wrapperClass){
		this.wrapperClass = wrapperClass;
		this.concretClass = concretClass;
	}
	
	/**
	 * Whether this {@link GSEnumDataType} is numerical or not
	 * 
	 * @return
	 */
	public boolean isNumericValue() {
		return concretClass.getSuperclass().equals(Number.class);
	}
	
	/**
	 * Return the inner type this data type encapsulate
	 * 
	 * @return
	 */
	public Class<?> getInnerType(){
		return concretClass;
	}
	
	/**
	 * Returns wrapper Gen* class
	 * 
	 * @see IValue
	 * 
	 * @return
	 */
	public Class<? extends IValue> getGenstarType(){
		return wrapperClass;
	}

	public static GSEnumDataType getType(Class<? extends IValue> clazz) {
		Optional<GSEnumDataType> opt = Arrays.asList(GSEnumDataType.values()).stream()
				.filter(type -> type.getGenstarType().equals(clazz)).findAny();
		if(opt.isPresent())
			return opt.get();
		throw new IllegalArgumentException(clazz.getCanonicalName()+" is not linked to any "
			+GSEnumDataType.class.getCanonicalName());
	}
	
}
