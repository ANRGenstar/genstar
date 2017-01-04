package spll.datamapper.matcher;

import core.metamodel.geo.AGeoEntity;
import spll.datamapper.variable.ISPLVariable;

public interface ISPLMatcher<V extends ISPLVariable, T> {

	public String getName();
	
	public T getValue();
	
	public boolean expandValue(T expand);
	
	public V getVariable();

	public AGeoEntity getEntity();
	
	public String toString();

}
