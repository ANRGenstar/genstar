package io.datareaders;

public enum DataType {

	Double (Double.class, "0d"),
	Integer (Integer.class, "0"),
	Boolean (Boolean.class, "true"),
	String (String.class, "null");

	private Class<?> wrapperClass;
	private java.lang.String defaultValue;

	private DataType(Class<?> wrapperClass, String defaultValue){
		this.wrapperClass = wrapperClass;
		this.defaultValue = defaultValue;
	}
	
	/**
	 * Whether this {@link DataType} is numerical or not
	 * 
	 * @return
	 */
	public boolean isNumericValue() {
		return wrapperClass.getSuperclass().equals(Number.class);
	}

	/**
	 * Default string value associated with this {@link DataType} 
	 * 
	 * @return
	 */
	public String getDefaultValue() {
		return defaultValue;
	}
	
}
