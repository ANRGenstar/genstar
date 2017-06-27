package gospl.algo.bn;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractInferenceEngine<N extends FiniteNode<N>> {

	private Logger logger = LogManager.getLogger();

	protected final BayesianNetwork<N> bn;
	
	/**
	 * evidence
	 */
	protected Map<N,String> variable2value = new HashMap<>();
	
	/**
	 * should we recompute probabilities ?
	 */
	protected boolean dirty = true;
	
	
	public AbstractInferenceEngine(BayesianNetwork<N> bn) {
		this.bn = bn;
		
		
	}

	/**
	 * Adds evidence, in the form p(n=s)=1 | XXX. 
	 * If there was another evidence for this node, it will be replaced.
	 * @param n
	 * @param s
	 */
	public void addEvidence(N n, String s) {
		
		if (!this.bn.containsNode(n))
			throw new IllegalArgumentException("this node is not in the bn: "+n);
		
		if (!n.getDomain().contains(s))
			throw new IllegalArgumentException("value "+s+" unknown in node "+n);
		
		dirty = (s != variable2value.put(n, s)) || dirty;
		
	}
	
	public void addEvidence(String n, String s) {
		N node = this.bn.getVariable(n);
		if (node == null)
			throw new IllegalArgumentException("unknown node "+n);
		this.addEvidence(node, s);
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
	

	/**
	 * Computes all the values in the Bayesien network.
	 * Note this is most of the time useless.
	 */
	public void computeAll() {
		for (N n: bn.nodes) {
			logger.info("computing probability for {} ({} values: {})", n, n.getDomainSize(), n.getDomain());
			retrieveConditionalProbability((NodeCategorical) n);	
			InferencePerformanceUtils.singleton.display();

		}
		
	}
	
	protected abstract BigDecimal retrieveConditionalProbability(NodeCategorical n, String s);
	
	protected abstract Map<String,BigDecimal> retrieveConditionalProbability(NodeCategorical n);

	
	public final BigDecimal getConditionalProbability(NodeCategorical n, String s) {
		if (!n.getDomain().contains(s))
			throw new IllegalArgumentException("there is no value "+s+" in the domain of variable "+n+" (use one of "+n.getDomain()+")");
		if (dirty)
			compute();
		return retrieveConditionalProbability(n, s);
	}
	
	/**
	 * Returns the conditional probability based on the initial probability distribution of the network 
	 * conditionned by evidence.
	 * @param variableName
	 * @param s
	 * @return
	 */
	public final BigDecimal getConditionalProbability(String variableName, String s) {
		NodeCategorical v = (NodeCategorical) bn.getVariable(variableName);
		if (v == null)
			throw new IllegalArgumentException("this Bayesian network does not contains a variable named "+variableName);
		return this.getConditionalProbability(v, s);
	}
	
	
}
