package spll.algo;

import java.util.Map;
import java.util.Set;

import io.datareaders.georeader.geodat.GSFeature;
import spll.datamapper.matcher.ISPLVariableFeatureMatcher;
import spll.datamapper.variable.ISPLVariable;

public interface ISPLRegressionAlgorithm<V extends ISPLVariable<?>, T> {
	
	public Map<V, Double> regression();
	
	public void setupData(Map<GSFeature, Double> observations,
			Set<ISPLVariableFeatureMatcher<V, T>> regressors);
		
}
