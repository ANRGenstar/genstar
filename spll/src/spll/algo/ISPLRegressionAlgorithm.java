package spll.algo;

import java.util.Map;

import spll.algo.variable.ISPLVariable;

public interface ISPLRegressionAlgorithm<V extends ISPLVariable<?>> {
	
	public Map<V, Double> regression();
		
}
