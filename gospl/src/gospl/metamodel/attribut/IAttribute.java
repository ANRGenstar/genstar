package gospl.metamodel.attribut;

import gospl.metamodel.attribut.value.IValue;
import io.datareaders.DataType;

/**
 * Attribute (of for instance an individual or household)
 * 
 * @author gospl-team
 *
 */
public interface IAttribute {

	public String getName();
	
	public DataType getDataType();
	
	public IValue getValue();
	
}
