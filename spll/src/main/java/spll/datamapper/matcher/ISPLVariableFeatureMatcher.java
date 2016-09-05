package spll.datamapper.matcher;

import io.datareaders.georeader.geodat.GSFeature;
import spll.datamapper.variable.ISPLVariable;

public interface ISPLVariableFeatureMatcher<V extends ISPLVariable<?>, T> {

	public String getName();
	
	public T getValue();
	
	public boolean expandValue(T expand);
	
	public V getVariable();

	public GSFeature getFeature();

}
