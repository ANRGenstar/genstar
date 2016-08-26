package gospl.metamodel.attribut.value;

import gospl.metamodel.attribut.IAttribute;
import io.datareaders.DataType;

public interface IValue {
	
	public String getInputStringValue();
	
	public IAttribute getAttribute();
	
	public DataType getDataType();
	
}
