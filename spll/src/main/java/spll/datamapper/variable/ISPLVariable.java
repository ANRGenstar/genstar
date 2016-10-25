package spll.datamapper.variable;

import core.io.geo.entity.attribute.value.AGeoValue;

public interface ISPLVariable {

	public AGeoValue getValue();
	
	public String getStringValue();
	
	public String getName();
	
}
