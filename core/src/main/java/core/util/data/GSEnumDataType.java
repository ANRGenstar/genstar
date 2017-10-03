package core.util.data;

/**
 * 
 * 
 * @author kevinchapuis
 * @author Vo Duc An
 *
 */
public enum GSEnumDataType {

	Double (Double.class, "0d"),
	Integer (Integer.class, "0"),
	Boolean (Boolean.class, "true"),
	String (String.class, "null");

	private Class<?> wrapperClass;
	private String defaultValue;

	private GSEnumDataType(Class<?> wrapperClass, String defaultValue){
		this.wrapperClass = wrapperClass;
		this.defaultValue = defaultValue;
	}
	
	/**
	 * Whether this {@link GSEnumDataType} is numerical or not
	 * 
	 * @return
	 */
	public boolean isNumericValue() {
		return wrapperClass.getSuperclass().equals(Number.class);
	}

	/**
	 * Default string value associated with this {@link GSEnumDataType} 
	 * 
	 * @return
	 */
	public String getDefaultValue() {
		return defaultValue;
	}
	
}
