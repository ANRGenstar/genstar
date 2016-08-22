package spll.algo.variable;

import org.opengis.feature.Feature;

public interface ISPLVariableFeatureMatcher<F extends Feature, V extends ISPLVariable<?>, T> {

	public String getName();
	
	public T getValue();
	
	public V getVariable();

	public F getFeature();

}
