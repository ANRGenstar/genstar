package spll.algo;

import java.util.Map;
import java.util.Set;

import core.metamodel.geo.AGeoEntity;
import core.metamodel.value.IValue;
import spll.datamapper.matcher.ISPLMatcher;
import spll.datamapper.variable.ISPLVariable;

public interface ISPLRegressionAlgo<V extends ISPLVariable, T> {
	
	public Map<V, Double> getRegressionParameter();
	
	public Map<AGeoEntity<? extends IValue>, Double> getResidual();
	
	public double getIntercept();
	
	public void setupData(Map<AGeoEntity<? extends IValue>, Double> observations,
			Set<ISPLMatcher<V, T>> regressors);
	
}
