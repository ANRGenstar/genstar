package gospl.metamodel.attribut;

import java.util.Set;

import gospl.metamodel.attribut.value.IValue;
import io.datareaders.DataType;

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
	 * The inner {@link DataType} that characterize this attribute' set of values 
	 * 
	 * @return the data type - {@link DataType}
	 */
	public DataType getDataType();
	
	/**
	 * The {@link IAttribute} this attribute target: should be itself, but could indicate disaggregated linked {@link IAttribute} or record linked one
	 * 
	 * @return
	 */
	public IAttribute getReferentAttribute();
	
	/**
	 * 
	 * @return
	 */
	public boolean isRecordAttribute();
	
// ------------------------- value related methods ------------------------- //
	
	public Set<IValue> getValues();

	public void addAll(Set<IValue> values);

	public void setEmptyValue(IValue emptyValue);
	
}
