package io.util.data;

public enum GSDataType {

	Double (Double.class, "0d"),
	Integer (Integer.class, "0"),
	Boolean (Boolean.class, "true"),
	String (String.class, "null");

	private Class<?> wrapperClass;
	private java.lang.String defaultValue;

	private GSDataType(Class<?> wrapperClass, String defaultValue){
		this.wrapperClass = wrapperClass;
		this.defaultValue = defaultValue;
	}
	
	/**
	 * Whether this {@link GSDataType} is numerical or not
	 * 
	 * @return
	 */
	public boolean isNumericValue() {
		return wrapperClass.getSuperclass().equals(Number.class);
	}

	/**
	 * Default string value associated with this {@link GSDataType} 
	 * 
	 * @return
	 */
	public String getDefaultValue() {
		return defaultValue;
	}
	
}
