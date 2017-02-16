package gospl.algo.bayesiannetworks;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractInferenceEngine {

	protected final BayesianNetwork bn;
	
	/**
	 * evidence
	 */
	protected Map<NodeCategorical,String> variable2value = new HashMap<>();
	
	/**
	 * should we recompute probabilities ?
	 */
	protected boolean dirty = true;
	
	
	public AbstractInferenceEngine(BayesianNetwork bn) {
		this.bn = bn;
		
		
	}

	/**
	 * Adds evidence, in the form p(n=s)=1 | XXX. 
	 * If there was another evidence for this node, it will be replaced.
	 * @param n
	 * @param s
	 */
	public void addEvidence(NodeCategorical n, String s) {
		
		if (!this.bn.containsNode(n))
			throw new IllegalArgumentException("this node is not in the bn: "+n);
		
		if (!n.getDomain().contains(s))
			throw new IllegalArgumentException("value "+s+" unknown in node "+n);
		
		dirty = (s != variable2value.put(n, s)) || dirty;
		
	}
	
	public void removeEvidence(NodeCategorical n) {
		dirty = variable2value.remove(n)!=null || dirty;
	}
	
	public void clearEvidence() {
		
		if (!variable2value.isEmpty())
			dirty = true;

		variable2value.clear();
	}
	
	public void compute() {
		
		
		// propagate probabilities (belief propagation)
		
		dirty = false;
		
	}
	
	protected abstract BigDecimal retrieveConditionalProbability(NodeCategorical n, String s);
	
	public final BigDecimal getConditionalProbability(NodeCategorical n, String s) {
		if (dirty)
			compute();
		return retrieveConditionalProbability(n, s);
	}
	
	
}
