package spll.algo;

import java.util.Map;
import java.util.Set;

import core.io.geo.entity.AGeoEntity;
import spll.datamapper.matcher.ISPLMatcher;
import spll.datamapper.variable.ISPLVariable;

public interface ISPLRegressionAlgo<V extends ISPLVariable, T> {
	
	public Map<V, Double> getRegressionParameter();
	
	public Map<AGeoEntity, Double> getResidual();
	
	public double getIntercept();
	
	public void setupData(Map<AGeoEntity, Double> observations,
			Set<ISPLMatcher<V, T>> regressors);
	
}
