package gospl.metamodel.attribut.value;

import gospl.metamodel.attribut.IAttribute;
import io.data.GSDataType;

public interface IValue {
	
	public String getInputStringValue();
	
	public IAttribute getAttribute();
	
	public GSDataType getDataType();
	
	public int hashCode();
	
	public boolean equals(Object obj);
	
}
