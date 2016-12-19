package spll.datamapper.matcher;

import spll.datamapper.variable.ISPLVariable;
import spll.entity.GSFeature;

public interface ISPLMatcher<V extends ISPLVariable, T> {

	public String getName();
	
	public T getValue();
	
	public boolean expandValue(T expand);
	
	public V getVariable();

	public GSFeature getFeature();
	
	public String toString();

}
