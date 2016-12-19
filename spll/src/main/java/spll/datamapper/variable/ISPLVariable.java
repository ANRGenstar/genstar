package spll.datamapper.variable;

import core.metamodel.geo.AGeoValue;

public interface ISPLVariable {

	public AGeoValue getValue();
	
	public String getStringValue();
	
	public String getName();
	
}
