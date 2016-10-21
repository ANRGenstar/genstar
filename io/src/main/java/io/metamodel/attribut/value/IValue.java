package io.metamodel.attribut.value;

import io.metamodel.attribut.IAttribute;
import io.util.data.GSEnumDataType;

public interface IValue {
	
	public String getStringValue();
	
	public String getInputStringValue();
	
	public IAttribute getAttribute();
	
	public GSEnumDataType getDataType();
	
	public int hashCode();
	
	public boolean equals(Object obj);
	
}
