package gospl.algo.sr.bn;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.map.LRUMap;
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

	private Map<NodeCategorical,double[]> computed = new HashMap<>();
	

	public SimpleConditionningInferenceEngine(CategoricalBayesianNetwork bn) {
		super(bn);

	}


	@Override
	protected double retrieveConditionalProbability(NodeCategorical n, String s) {
		
		logger.debug("p({}={}|{}", n.name, s, evidenceVariable2value);

		// can we even compute it ? 
		// TODO ???if (blacklisted.contains(n))
		//	throw new IllegalArgumentException("cannot compute the probability "+n+"="+s+" with this evidence "+variable2value+": the evidence is posterior this node, and this engine is not able to deal with backpropagation");
		
		// is it part of evidence ?
		{
			String ev = evidenceVariable2value.get(n);
			if (ev != null) {
				if (ev.equals(s))
					return 1.;
				else 
					return 0.;
			}
		}
		
		// did we computed it already ?
		double[] done = computed.get(n);
		// did we stored anything for this node ? (if not, prepare for it)
		if (done == null) {
			done = new double[n.getDomainSize()];
			Arrays.fill(done, -1.);
			computed.put(n, done);
		} else {
			double res = done[n.getDomainIndex(s)];
			if (res > 0)
				return res;
		}
		
		double res;

		// we did not computed this value.
		// maybe we know everything but this one ?
		int known = 0;
		for (int i=0; i<done.length; i++) {
			if (done[i] >= 0){ 
				known++;
			}
		}
		if (known == done.length-1) {
			// we know all the values but one
			logger.debug("we can save one computation here by doing p(X=x)=1 - sum(p(X=^x))");
			double total = 1.;
			for (double d : done) {
				if (d>=0)
					total -= d;
			}
			res = total;
		} else {
			logger.trace("no value computed for p({}={}|{}), starting computation...", n.name, s, evidenceVariable2value);
			//res = n.getConditionalProbabilityPosterior(s, variable2value, computed);
			res = computePosteriorConditionalProbability(n, s, evidenceVariable2value);
		}
		done[n.getDomainIndex(s)] = res;
		logger.trace("returning p({}={}|{})={}", n.name, s, evidenceVariable2value, res);

		return res;
		
	}
	

	@Override
	protected double[] retrieveConditionalProbability(NodeCategorical n) {
		
		double[] done;
	
		// is it part of evidence ?
		{
			String ev = evidenceVariable2value.get(n);
			if (ev != null) {
				done = new double[n.getDomainSize()];
				for (int i=0; i<n.getDomainSize(); i++) {
					String v = n.getValueIndexed(i);
					if (ev.equals(v)) {
						done[i] = 1.; 
						break; // we can break there in fact (initialized to 0 !)
					} else
						done[i] = 0.;
				}
				return done;

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
			
			done = computePosteriorConditionalProbability(n, evidenceVariable2value);
			
			computed.put(n, done);
		}
		logger.trace("returning p({}=*|{}) : {}", n.name, evidenceVariable2value, done);

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


	private LRUMap<Map<NodeCategorical,String>,Map<Set<NodeCategorical>,Double>> known2nuisance2value = new LRUMap<>();
	
	private Double getCached(
			Map<NodeCategorical,String> known, 
			Set<NodeCategorical> nuisance
			) {
		
		Map<Set<NodeCategorical>,Double> res = known2nuisance2value.get(known);
		if (res == null)
			return null;
		return res.get(nuisance);
		
	}
	
	private void storeCache(
			Map<NodeCategorical,String> known, 
			Set<NodeCategorical> nuisance,
			Double d
			) {
		Map<Set<NodeCategorical>,Double> res = known2nuisance2value.get(known);
		if (res == null) {
			res = new HashMap<>();
			known2nuisance2value.put(known, res);
		}
		res.put(nuisance, d);
	}
	
	/**
	 * Given a set of known values for variables, and the list of the remaining variables not 
	 * covered by this evidence (refered to as nuisance variables),
	 * sums probabilities over the relevant variables to computed the expected probability.
	 * @param known
	 * @param node2probabilities 
	 */
	protected double sumProbabilities(
			Map<NodeCategorical,String> known, 
			Set<NodeCategorical> nuisanceRaw) {
		
		
		Set<NodeCategorical> nuisanceS = new HashSet<>(nuisanceRaw);
		nuisanceS.removeAll(known.keySet());
		
		// quick exit
		if (nuisanceRaw.isEmpty() && known.isEmpty())
			return 1.;
		
		// is it cached ?
		Double res = getCached(known, nuisanceS); // optimisation: cache !
		if (res != null)
			return res;
		
		
		res = 0.;
						
		logger.debug("summing probabilities for nuisance {}, and known {}", known, nuisanceS);

		for (IteratorCategoricalVariables it = bn.iterateDomains(nuisanceS); it.hasNext(); ) {
			
			Map<NodeCategorical,String> n2v = it.next();
			n2v.putAll(known);
			
			double p = this.bn.jointProbability(n2v, Collections.emptyMap());
			
			logger.info("p({})={}", n2v, p);
			
			res += p;
			InferencePerformanceUtils.singleton.incAdditions();

			// if over one, stop.
			if (res >= 1) {
				res = 1.;
				break;
			}

		}
		
		
		storeCache(known, nuisanceS, res);
		
		logger.debug("total {}", res);
		return res;
	}
	
	
	/**
	 * For a given node, computes the probabilities accounting prior probabilities 
	 * and evidence (of parents or children).
	 * Returns the probabilities for each value of the domain.
	 * Only computes if the value is not already present in cache 
	 * @param n
	 */
	protected double[] computePosteriorConditionalProbability(
											NodeCategorical n, 
											Map<NodeCategorical,String> evidence) {
		
		double[] v2p = new double[n.getDomainSize()];

		double pFree = this.sumProbabilities(evidence, selectRelevantVariables((NodeCategorical)null, evidence, bn.nodes)); // optimisation: elimination of irrelevant variables

		for (int i=0; i<n.getDomainSize(); i++) {
			String nv = n.getValueIndexed(i);
		
			logger.debug("computing p(*=*|{}={})", n.name, nv);
							
			Map<NodeCategorical,String> punctualEvidence = new HashMap<>(evidence);
			punctualEvidence.put(n, nv);
						
			double p = this.sumProbabilities(
					punctualEvidence, 
					selectRelevantVariables(n, evidence, bn.nodes) // optimisation: elimination of irrelevant variables
					);
						
			logger.debug("computed p({}={}|{},{}={})={}", n.name, nv, punctualEvidence, n.name, nv, p);
			
			logger.debug("computed p(*=*|{}={})={}", n.name, nv, p);
			v2p[i] = p;
			
		}
		
		logger.debug("now computing the overall probas");

		for (int i=0; i<n.getDomainSize(); i++) {
			String nv = n.getValueIndexed(i);

			double p = v2p[i];
			
			double pp = p / pFree;
			v2p[i] = pp;
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
	protected double computePosteriorConditionalProbability(
											NodeCategorical n, 
											String nv,
											Map<NodeCategorical,String> evidence) {
						
		double pFree = this.sumProbabilities(evidence, selectRelevantVariables((NodeCategorical)null, evidence, bn.nodes)); // optimisation: elimination of irrelevant variables

		logger.debug("computing p(*=*|{}={})", n.name, nv);
						
		Map<NodeCategorical,String> punctualEvidence = new HashMap<>(evidence);
		punctualEvidence.put(n, nv);
					
		double p = this.sumProbabilities(
				punctualEvidence, 
				selectRelevantVariables(n, evidence, bn.nodes) // optimisation: elimination of irrelevant variables
				);
					
		logger.debug("computed p({}={}|{},{}={})={}", n.name, nv, punctualEvidence, n.name, nv, p);
		
		logger.debug("computed p(*=*|{}={})={}", n.name, nv, p);
					
		logger.debug("now computing the overall probas");

		
		double pp;
		try {
			pp = p / pFree;
		} catch (ArithmeticException e) {
			logger.error("unable to compute probability p({}={}|*): pfree={}, p={}", n.name, nv, pFree, p);
			pp = 0.; // TODO ???
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


	@Override
	public double getProbabilityEvidence() {

		// quick answer
		if (evidenceVariable2value.isEmpty())
			return 1.;
		
		return this.sumProbabilities(evidenceVariable2value, selectRelevantVariables((NodeCategorical)null, evidenceVariable2value, bn.nodes)); // optimisation: elimination of irrelevant variables

	}



	
	
}
