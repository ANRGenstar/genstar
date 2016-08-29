package gospl.metamodel.attribut;

import java.util.Set;

import gospl.metamodel.attribut.value.IValue;
import io.data.GSDataType;

/**
 * Attribute (of for instance an individual or household)
 * 
 * @author gospl-team
 *
 */
public interface IAttribute {

	/**
	 * The name of the attribute
	 * 
	 * @return the name - {@link String}
	 */
	public String getName();
	
	/**
	 * The inner {@link GSDataType} that characterize this attribute' set of values 
	 * 
	 * @return the data type - {@link GSDataType}
	 */
	public GSDataType getDataType();
	
	/**
	 * The {@link IAttribute} this attribute target: should be itself, but could indicate disaggregated linked {@link IAttribute} or record linked one
	 * 
	 * @return
	 */
	public IAttribute getReferentAttribute();
	
	/**
	 * A record attribute represents a purely utility attribute (for instance, the number of agent of age 10)
	 * 
	 * @return
	 */
	public boolean isRecordAttribute();
	
// ------------------------- value related methods ------------------------- //
	
	/**
	 * The {@link IValue}s this {@link IAttribute} have as possible value.
	 * 
	 * @return
	 */
	public Set<IValue> getValues();

	/**
	 * 
	 * If the value set for this {@link IAttribute} is empty then values could be set to the ones in parameter. 
	 * Otherwise, values could not be change. 
	 * 
	 * @param values
	 * @return <code>true</code> if it actually set values, <code>false</code> if not 
	 */
	public boolean setValues(Set<IValue> values);

	/**
	 * The empty default {@link IValue} for this {@link IAttribute}
	 * 
	 * @param emptyValue
	 */
	public void setEmptyValue(IValue emptyValue);
	
}
