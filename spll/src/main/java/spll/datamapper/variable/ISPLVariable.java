package spll.datamapper.variable;

import io.data.geo.attribute.IGeoValue;

public interface ISPLVariable {

	public IGeoValue getValue();
	
	public String getStringValue();
	
	public String getName();
	
}
