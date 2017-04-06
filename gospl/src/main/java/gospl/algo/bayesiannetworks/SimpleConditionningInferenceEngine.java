package gospl.algo.bayesiannetworks;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Simple conditioning stands as the simplest inference engine possible to propagate evidence 
 * in Bayesian Networks. It just compute posterior probabilities of every variable based on 
 * either its original probabilities, or based on evidence on the variable or on one parent of the variable. 
 * 
 * This inference engine suffers many limitations, including: evidence can only be taken into account from
 * the roots to the leaves (in the natural direction of conditionning) with no backpropagation.
 * If you query conditional probabilities on an ancestor of a conditioned node, you will receive an 
 * IllegalArgumentException. 
 * Also, if you introduce incompatible pieces of evidence which conflict each other, the engine will simply not
 * detect it. 
 * 
 * 
 * @author Samuel Thiriot
 *
 */
public class SimpleConditionningInferenceEngine extends AbstractInferenceEngine {

	
	private Map<NodeCategorical,Map<String,BigDecimal>> computed = new HashMap<>();
	
	public SimpleConditionningInferenceEngine(BayesianNetwork bn) {
		super(bn);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void compute() {
		
		computed.clear();
		
		// TODO can we detect easily conflicting evidence ?
		
		// no evidence can be taken into account for nodes ancestors to a node with evidence
		Set<NodeCategorical> blacklisted = new HashSet<>();
		for (NodeCategorical n: bn.enumerateNodes()) {
			if (variable2value.containsKey(n)) {
				blacklisted.addAll(n.getParents());
			}
		}
		
		// let's start we the evidence we have: this evidence means that we have a p(V=d|...)=1
		for (NodeCategorical n: variable2value.keySet()) {
			
			if (blacklisted.contains(n))
				continue;
			
			String v = variable2value.get(n);
			
			// we know the value for this one !
			if (!computed.containsKey(n))
				computed.put(n, new HashMap<>());
			// for sure we have p(n=v)=1
			computed.get(n).put(v, BigDecimal.ONE);
			// and p(n!=v) = 0
			for (String v2: n.getDomain()) {
				if (v2.equals(v)) {
					continue;
				}
				computed.get(n).put(v2, BigDecimal.ZERO);
			}
		}
		
		// let's continue with the probabilities we can now compute
		for (NodeCategorical n: bn.enumerateNodes()) {
			
			if (blacklisted.contains(n))
				continue;
			
			// don't compute nodes with direct evidence !
			if (variable2value.containsKey(n))
				continue;
				
			if (!computed.containsKey(n))
				computed.put(n, new HashMap<>());
			
			for (String v : n.getDomain()) {
			
				// compute based on its parents
				computed.get(n).put(v, n.getConditionalProbabilityPosterior(v, variable2value));
				
			}
			
		}
		
		// mark it clean
		super.compute();
		
	}

	@Override
	protected BigDecimal retrieveConditionalProbability(NodeCategorical n, String s) {
		try {
			return computed.get(n).get(s);
		} catch (NullPointerException e) {
			throw new IllegalArgumentException("cannot compute the probability "+n+"="+s+" with this evidence "+variable2value);
		}
	}


	
	
}
