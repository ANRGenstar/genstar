package spll.datamapper.variable;

import core.metamodel.value.geo.IValue;

public interface ISPLVariable {

	public IValue getValue();
	
	public String getStringValue();
	
	public String getName();
	
}
