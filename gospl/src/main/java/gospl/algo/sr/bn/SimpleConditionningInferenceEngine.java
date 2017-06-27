package gospl.algo.sr.bn;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Simple conditioning stands as the simplest exact inference engine possible to propagate evidence 
 * in Bayesian Networks. Can only be used on small Bayesian networks - is untractable on bigger ones. 
 * Use it one simple cases, or as a benchmark. Was validated against test networks and inference engines in samiam. 
 * 
 * 
 * It just compute posterior probabilities of every variable based on 
 * either its original probabilities, or based on evidence on the variable or on one parent of the variable. 
 * 
 * Only computes probabilities on demand, so few demands will lead to few computations.
 * Caches all the computed probabilities so many demands will not increase computations anymore.
 * 
 * Simple optimizations were implemented, including: <ul>
 * <li>excluding irrelevant variables during computation</li>
 * <li>stop multiplications as soon as a zero is met</li>
 * <li>select order of variables based on their likelihood of bringing zeros</li>
 * <li>do not compute the last probability of a domain but rely of the complement to 1 instead</li>
 * <li>Cache normalization factors, which corresponds to variable elimination</li>
 * </ul>
 * 
 * @author Samuel Thiriot
 *
 */
public class SimpleConditionningInferenceEngine extends AbstractInferenceEngine {

	private Logger logger = LogManager.getLogger();

	private Map<NodeCategorical,Map<String,BigDecimal>> computed = new HashMap<>();
	

	public SimpleConditionningInferenceEngine(CategoricalBayesianNetwork bn) {
		super(bn);

	}


	@Override
	protected BigDecimal retrieveConditionalProbability(NodeCategorical n, String s) {
		
		logger.debug("p({}={}|{}", n.name, s, variable2value);

		// can we even compute it ? 
		// TODO ???if (blacklisted.contains(n))
		//	throw new IllegalArgumentException("cannot compute the probability "+n+"="+s+" with this evidence "+variable2value+": the evidence is posterior this node, and this engine is not able to deal with backpropagation");
		
		// is it part of evidence ?
		{
			String ev = variable2value.get(n);
			if (ev != null) {
				if (ev.equals(s))
					return BigDecimal.ONE;
				else 
					return BigDecimal.ZERO;
			}
		}
		
		// did we computed it already ?
		Map<String,BigDecimal> done = computed.get(n);
		BigDecimal res = null;
		// did we stored anything for this node ? (if not, prepare for it)
		if (done == null) {
			done = new HashMap<>();
			computed.put(n, done);
		} else {
			res = done.get(s);
		}
		
		// did we computed that specific one ? if not, compute it
		if (res == null) {
			// did we already computed everything else than this single value? 
			if (done.size() == n.getDomainSize()-1) {
				logger.debug("we can save one computation here by doing p(X=x)=1 - sum(p(X=^x))");
				BigDecimal total = BigDecimal.ONE;
				for (BigDecimal d : done.values()) {
					total = total.subtract(d);
				}
				res = total;
			} else {
				logger.trace("no value computed for p({}={}|{}), starting computation...", n.name, s, variable2value);
				//res = n.getConditionalProbabilityPosterior(s, variable2value, computed);
				res = computePosteriorConditionalProbability(n, s, variable2value);
			}
			done.put(s, res);
		}
		logger.trace("returning p({}={}|{})={}", n.name, s, variable2value, res);

		return res;
		
	}
	

	@Override
	protected Map<String, BigDecimal> retrieveConditionalProbability(NodeCategorical n) {
		
		Map<String, BigDecimal> done = null;
	
		// is it part of evidence ?
		{
			String ev = variable2value.get(n);
			if (ev != null) {
				done = new HashMap<>(n.getDomainSize());
				for (String v: n.getDomain()) {
					if (ev.equals(v)) {
						done.put(v, BigDecimal.ONE);
					} else
						done.put(v, BigDecimal.ZERO);
				}
			}
		}
		
		// did we computed it already ?
		done = computed.get(n);
		// did we stored anything for this node ? (if not, prepare for it)
		if (done != null) {
			return done;
		}
		
		// did we computed that specific one ? if not, compute it
		if (done == null) {
			
			done = computePosteriorConditionalProbability(n, variable2value);
			
			computed.put(n, done);
		}
		logger.trace("returning p({}=*|{}) : {}", n.name, variable2value, done);

		return done;
		
	}


	/**
	 * 
	 * @param nodes
	 */
	protected Set<NodeCategorical> getLeaf(Set<NodeCategorical> nodes) {
		
		logger.debug("searching for the leafs of {}", nodes);
		Set<NodeCategorical> leafs = new HashSet<>(nodes);
		
		for (NodeCategorical n: nodes) {
			leafs.removeAll(n.getParents());
		}
		
		logger.debug("leafs of {} are {}", nodes, leafs);

		return leafs;
		
	}
	
	/**
	 * Given a set of known values for variables, lists all the combinations of variables / value to be investigated 
	 * @param known
	 */
	protected List<Map<NodeCategorical,String>> enumerateNuisanceVariableValues(Map<NodeCategorical,String> known, Set<NodeCategorical> nuisanceS) {
		
		List<Map<NodeCategorical,String>> res = new LinkedList<>();
		
		// the nodes we have to explore the values for 
		NodeCategorical[] nuisance = new NodeCategorical[nuisanceS.size()];
		nuisance = nuisanceS.toArray(nuisance);
		
		// the current values we are exploring for each node
		int[] nodeIdx2valueIdx = new int[nuisance.length];
		
		main: while (true) {
			
			// store the current explored values in a map
			Map<NodeCategorical,String> n2v = new HashMap<>(nuisance.length);
			for (int i=0; i<nodeIdx2valueIdx.length; i++) {
				n2v.put(nuisance[i], nuisance[i].getValueIndexed(nodeIdx2valueIdx[i]));
			}
			logger.debug("should explore: {}", n2v);
			res.add(n2v);
			
			// skip to the next index
			int cursorParents = nodeIdx2valueIdx.length-1;
			nodeIdx2valueIdx[cursorParents]++;
			// ... if we are at the max of the domain size of the current node, then shift back
			while (nodeIdx2valueIdx[cursorParents]>=nuisance[cursorParents].getDomainSize()) {
				nodeIdx2valueIdx[cursorParents] = 0;
				cursorParents--;
				// maybe we finished the exploration ?
				if (cursorParents < 0) 
					break main;
				// skip to the next one 
				nodeIdx2valueIdx[cursorParents]++;
			}
		}
		
		return res;
	}

	private Map<Map<NodeCategorical,String>,Map<Set<NodeCategorical>,BigDecimal>> known2nuisance2value = new HashMap<>();
	
	private BigDecimal getCached(
			Map<NodeCategorical,String> known, 
			Set<NodeCategorical> nuisance
			) {
		
		Map<Set<NodeCategorical>,BigDecimal> res = known2nuisance2value.get(known);
		if (res == null)
			return null;
		return res.get(nuisance);
		
	}
	
	private void storeCache(
			Map<NodeCategorical,String> known, 
			Set<NodeCategorical> nuisance,
			BigDecimal d
			) {
		Map<Set<NodeCategorical>,BigDecimal> res = known2nuisance2value.get(known);
		if (res == null) {
			res = new HashMap<>();
			known2nuisance2value.put(known, res);
		}
		res.put(nuisance, d);
	}
	
	/**
	 * Given a set of known values for variables, lists all the combinations of variables / value to be investigated 
	 * @param known
	 * @param node2probabilities 
	 */
	protected BigDecimal sumProbabilities(
			Map<NodeCategorical,String> known, 
			Set<NodeCategorical> nuisanceRaw) {
		
		
		Set<NodeCategorical> nuisanceS = new HashSet<>(nuisanceRaw);
		nuisanceS.removeAll(known.keySet());
		
		BigDecimal res = getCached(known, nuisanceS); // optimisation: cache !
		if (res != null)
			return res;
		
		res = BigDecimal.ZERO;
				
		logger.debug("summing probabilities for nuisance {}, and known {}", known, nuisanceS);
		
		// the nodes we have to explore the values for 
		NodeCategorical[] nuisance = new NodeCategorical[nuisanceS.size()];
		nuisance = nuisanceS.toArray(nuisance);
		
		// the current values we are exploring for each node
		int[] nodeIdx2valueIdx = new int[nuisance.length];
		
		main: while (true) {
			
			// store the current explored values in a map
			Map<NodeCategorical,String> n2v = new HashMap<>(known);
			for (int i=0; i<nodeIdx2valueIdx.length; i++) {
				n2v.put(nuisance[i], nuisance[i].getValueIndexed(nodeIdx2valueIdx[i]));
			}
			
			BigDecimal p = this.bn.jointProbability(n2v, Collections.emptyMap());
			
			logger.debug("p({})={}", n2v,p);
			
			res = res.add(p);
			InferencePerformanceUtils.singleton.incAdditions();
			
			if (nodeIdx2valueIdx.length == 0)
				break;
			// skip to the next index
			int cursorParents = nodeIdx2valueIdx.length-1;
			nodeIdx2valueIdx[cursorParents]++;
			// ... if we are at the max of the domain size of the current node, then shift back
			while (nodeIdx2valueIdx[cursorParents]>=nuisance[cursorParents].getDomainSize()) {
				nodeIdx2valueIdx[cursorParents] = 0;
				cursorParents--;
				// maybe we finished the exploration ?
				if (cursorParents < 0) 
					break main;
				// skip to the next one 
				nodeIdx2valueIdx[cursorParents]++;
			}
		}
		
		storeCache(known, nuisanceS, res);
		
		logger.debug("total {}", res);
		return res;
	}
	
	/**
	 * Selects the variables relevant to assess the probability of node toCompute 
	 * knowing evidence and all the nodes that might be considered.
	 * @param toCompute
	 * @param evidence
	 * @param all
	 * @return
	 */
	private Set<NodeCategorical> selectRelevantVariables(
			NodeCategorical toCompute,
			Map<NodeCategorical,String> evidence,
			Set<NodeCategorical> all
			) {
		Set<NodeCategorical>  relevant = new HashSet<>(all.size());
		
		if (toCompute != null)
			relevant.addAll(toCompute.getAllAncestors());
		
		for (NodeCategorical n: evidence.keySet()) {
			relevant.addAll(n.getAllAncestors());
		}
		
		return relevant;
	}
	
	
	/**
	 * For a given node, computes the probabilities accounting prior probabilities 
	 * and evidence (of parents or children).
	 * Returns the probabilities for each value of the domain.
	 * Only computes if the value is not already present in cache 
	 * @param n
	 */
	protected Map<String,BigDecimal> computePosteriorConditionalProbability(
											NodeCategorical n, 
											Map<NodeCategorical,String> evidence) {
		
		Map<String,BigDecimal> v2p = new HashMap<>(n.getDomainSize());
				
		BigDecimal pFree = this.sumProbabilities(evidence, selectRelevantVariables(null, evidence, bn.nodes)); // optimisation: elimination of irrelevant variables

		for (String nv: n.getDomain()) {
			
			logger.debug("computing p(*=*|{}={})", n.name, nv);
							
			Map<NodeCategorical,String> punctualEvidence = new HashMap<>(evidence);
			punctualEvidence.put(n, nv);
						
			BigDecimal p = this.sumProbabilities(
					punctualEvidence, 
					selectRelevantVariables(n, evidence, bn.nodes) // optimisation: elimination of irrelevant variables
					);
						
			logger.debug("computed p({}={}|{},{}={})={}", n.name, nv, punctualEvidence, n.name, nv, p);
			
			logger.debug("computed p(*=*|{}={})={}", n.name, nv, p);
			v2p.put(nv, p);
			
		}
		
		logger.debug("now computing the overall probas");

		for (String nv : n.getDomain()) {
			BigDecimal p = v2p.get(nv);
			BigDecimal pp = p.divide(pFree, BigDecimal.ROUND_HALF_UP);
			v2p.put(nv, pp);
			logger.debug("computed p({}={}|evidence)= p({}={}|evidence)/p({}|evidence)={}/{}={}", n.name, nv, n.name, nv, n.name, p, pFree, pp);
		}
		
		
		return v2p;
	}
	
	

	/**
	 * For a given node and a given value in its discrete domain, computes its probability accounting 
	 * evidence and prior probailities 
	 * already computed beforehand.
	 * At the end, returns the probabilities for each value of the domain. 
	 * @param n
	 */
	protected BigDecimal computePosteriorConditionalProbability(
											NodeCategorical n, 
											String nv,
											Map<NodeCategorical,String> evidence) {
						
		BigDecimal pFree = this.sumProbabilities(evidence, selectRelevantVariables(null, evidence, bn.nodes)); // optimisation: elimination of irrelevant variables

		
		logger.debug("computing p(*=*|{}={})", n.name, nv);
						
		Map<NodeCategorical,String> punctualEvidence = new HashMap<>(evidence);
		punctualEvidence.put(n, nv);
					
		BigDecimal p = this.sumProbabilities(
				punctualEvidence, 
				selectRelevantVariables(n, evidence, bn.nodes) // optimisation: elimination of irrelevant variables
				);
					
		logger.debug("computed p({}={}|{},{}={})={}", n.name, nv, punctualEvidence, n.name, nv, p);
		
		logger.debug("computed p(*=*|{}={})={}", n.name, nv, p);
					
		logger.debug("now computing the overall probas");

		
		BigDecimal pp = null;
		try {
			pp = p.divide(pFree, BigDecimal.ROUND_HALF_UP);
		} catch (ArithmeticException e) {
			logger.error("unable to compute probability p({}={}|*): pfree={}, p={}", n.name, nv, pFree, p);
			pp = BigDecimal.ZERO; // TODO ???
		}
		
		
		logger.debug("computed p({}={}|evidence)= p({}={}|evidence)/p({}|evidence)={}/{}={}", n.name, nv, n.name, nv, n.name, p, pFree, pp);
		
		return pp;
	}
	

	
	
	@Override
	public void compute() {
				
		computed.clear();


		// TODO can we detect easily conflicting evidence ?
		

		// mark it clean
		super.compute();
		
	}



	
	
}
