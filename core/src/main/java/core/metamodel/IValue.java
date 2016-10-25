package core.metamodel;

/**
 * The values that characterise Genstar's attribute of entity
 * 
 * @author kevinchapuis
 *
 */
public interface IValue {
	
	/**
	 * The value represented as a String
	 * 
	 * @return
	 */
	public String getStringValue();
	
	/**
	 * The value as it has been input from the data into this object
	 * 
	 * @return
	 */
	public String getInputStringValue();
	
	/**
	 * The attribute this value refers to
	 * 
	 * @return
	 */
	public IAttribute<? extends IValue> getAttribute();
	
	/**
	 * Force to overload hashcode method to ensure equals consistency
	 * 
	 * @return
	 */
	public int hashCode();
	
	/**
	 * Force to overload equals method
	 * 
	 * @param obj
	 * @return
	 */
	public boolean equals(Object obj);
	
}
