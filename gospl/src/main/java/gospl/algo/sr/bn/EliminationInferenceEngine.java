package gospl.algo.sr.bn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.util.random.GenstarRandom;

// TODO caching of intermediate factors !
// TODO sampleOne: maybe we can create bigger factors then randomly pick up a value with a roulette in the entire factor? 

public class EliminationInferenceEngine extends AbstractInferenceEngine {

	private Logger logger = LogManager.getLogger();
	
	//private Map<NodeCategorical,Factor> node2factorForEvidence = null;
	private List<NodeCategorical> eliminationOrderForEvidence = null;
	
	private Map<NodeCategorical,Factor> factorsForEvidence = null;
	
	private LRUMap<Set<NodeCategorical>,Factor> cacheNode2factorForEvidence = null;
	
	public EliminationInferenceEngine(CategoricalBayesianNetwork bn) {
		super(bn);
		cacheNode2factorForEvidence = new LRUMap<>(bn.getNodes().size()*100); // purely arbitrary
	}
	
	
	// TODO : only on a subset of variables
	// we should compute the multiplication of factors one
	// and only multiply with the other factors concerned by evidence
	/**
	 * returns the factors related to the variables passed as parameter. 
	 * These are the factors that cannot be loaded from cache as they depend from these variables
	 * @param vars
	 * @return
	 */
	protected Set<Factor> getFactorsWithVariables(Set<NodeCategorical> vars) {
		Set<Factor> res = new HashSet<>(vars.size());
		
		for (NodeCategorical n: vars) {
			res.add(bn.getFactor(n));
		}
		
		return res;
	}

	protected List<NodeCategorical> getEliminationOrderOptimalForZero(Set<NodeCategorical> remainingVariables) {

		List<NodeCategorical> sorted = new ArrayList<>(remainingVariables);

		// quick exit
		if (remainingVariables.isEmpty())
			return sorted;
		
		// TODO not sure this is an ideal order 
		// it is made of both the enumeration order and its rectification with the cardinality
		// tests are ok 
		
		Collections.sort(sorted, new Comparator<NodeCategorical>() {

			/*
			protected int computeCardinality(NodeCategorical v) {
				
				if (evidenceVariable2value.containsKey(v))
					return v.getParentsCardinality(); 
				
				int card2 = v.getCardinality();
				for (NodeCategorical p:  v.getParents()) {
					if (evidenceVariable2value.containsKey(p)) {
						card2 = card2 / p.getDomainSize();
					}
				}
				
				return card2;
			}
			*/
			
			@Override
			public int compare(NodeCategorical o1, NodeCategorical o2) {
				
				//return computeCardinality(o2) - computeCardinality(o1);
				
				// if no evidence ?
				return o2.getCardinality() - o1.getCardinality();
			}
			
		});
		
		return sorted;
		
	}

	@Override
	public Factor computeFactorPosteriorMarginals(Set<NodeCategorical> variables) {
		
		Factor f = cacheNode2factorForEvidence.get(variables);
		
		if (f != null) {
			InferencePerformanceUtils.singleton.incCacheHit();
			return f;
		}
		InferencePerformanceUtils.singleton.incCacheMiss();
		
		// TODO efficient order !
		LinkedHashSet<NodeCategorical> remainingVariables = new LinkedHashSet<>(bn.enumerateNodes());
		
		// optimisation: only focus on relevant variables
		remainingVariables.retainAll(selectRelevantVariables(variables, evidenceVariable2value, bn.getNodes()));

		Set<NodeCategorical> all = new HashSet<>(remainingVariables);
		remainingVariables.removeAll(variables);

		// Collections.shuffle(sorted);
				
		f = computeFactorPosteriorMarginals(all, getEliminationOrderOptimalForZero(remainingVariables));
		
		cacheNode2factorForEvidence.put(variables, f);

		return f;
	}
	
	/*
	public Factor eliminationAsk(NodeCategorical variable) {

		Factor f = null;
		
		List<NodeCategorical> variables = new LinkedList<>(bn.enumerateNodes());
		Collections.reverse(variables);

		for (NodeCategorical n: variables) {
			if (f == null)
				f = n.asFactor().reduction(evidenceVariable2value);
			else 
				f = n.asFactor().reduction(evidenceVariable2value).multiply(f);
				
			if (n != variable && f.variables.contains(n)) 
				f.sumOut(n);
			
		}
		
		f.normalize();
		
		return f;
	}*/
	
	/**
	 * Computes prior (that is, without evidence) probabilities in the form of a factor. 
	 * Uses CPT as factors and elimination. 
	 * @param variables
	 * @param orderOtherVariables
	 * @return
	 */
	protected Factor computeFactorPosteriorMarginals(
			Set<NodeCategorical> variables, 
			List<NodeCategorical> orderOtherVariables) {
		
		int biggestCPT = 0;
		
		logger.debug("elimination on {} with order {}", variables, orderOtherVariables);

		// are parameters ok ? 
		/*{
			
			// order on other variables should also be in BN
			Set<NodeCategorical> other = new HashSet<>(orderOtherVariables);
			if (bn.getNodes().size() != variables.size()+other.size()) {
				throw new IllegalArgumentException("expecting variables U orderOtherVariables = bn.variables");
			}
			
		}*/
		
		Set<NodeCategorical> all = new HashSet<>(orderOtherVariables);
		all.addAll(variables);
		
		// TODO use only the relevant factors !
		
		// TODO cache !


		Map<NodeCategorical,Factor> node2factor = all.stream().collect(Collectors.toMap(
																m->m, 
																m->bn.getFactor(m).reduction(evidenceVariable2value)
																));
		
		if (logger.isDebugEnabled())
			logger.debug("reduced: {}", node2factor.entrySet().stream().map(e->e.getKey().name+":"+e.getValue().toStringLong()).collect(Collectors.joining(",")));

		
		for (NodeCategorical n: orderOtherVariables) {
			logger.debug("processing {}", n);
			Factor f = null;
			
			// product of all relevant factors
			logger.debug("product for {}", n);
			for (NodeCategorical m: all ) { // bn.getNodes()
				if (!node2factor.containsKey(m))
					continue;
				
				Factor f2 = node2factor.get(m);
				if (!f2.variables.contains(n))
					continue;
				
				if (f == null)
					f = f2;
				else {
					if (logger.isDebugEnabled())
						logger.debug("mult {} X {}", f.toStringLong(), f2.toStringLong());
					f = f.multiply(f2);
					if (logger.isDebugEnabled())
						logger.debug("={}", f.toStringLong());
				}
				node2factor.remove(m);
			}
			
			//if (f == null)
			//	f = new Factor(bn, Collections.emptySet());
			
			if (f==null) {
				// there was nothing to compute with this variable !
				// let's remove it 
				 //node2factor.remove(f); // TODO???
			} else {
				biggestCPT = Math.max(biggestCPT, f.values.size());
				if (logger.isDebugEnabled())
					logger.debug("sum {} for {}", n.name, f.toStringLong());

				// sum of all relevant factors
				f = f.sumOut(n);
				if (logger.isDebugEnabled())
					logger.debug("={}", f.toStringLong());

				// now replace fk by fi
				node2factor.put(n, f);
			}
			
			
			
			
			//node2factor.remove(n);
			//logger.debug("put {} => {}", n, f);

			//logger.debug("computed {}", f.toStringLong());

			/*
			for (NodeCategorical m: bn.getNodes()) {
				Factor f2 = node2factor.get(m);
				if (!f2.variables.contains(n))
					continue;
				//Factor novel = f2.multiply(f).sumOut(n);
				logger.debug("put for {}: {}", m , f);
				node2factor.put(m, f);
			}*/
			
		}
		
		Factor res = new Factor(bn, Collections.emptySet());
		for (Factor f: node2factor.values()) {
			if (res == null)
				res = f;
			else {
				//logger.debug(res.toStringLong());
				if (logger.isDebugEnabled())
					logger.debug("mult {} X {}", res, f.toStringLong());
				res = res.multiply(f);
				if (logger.isDebugEnabled())
					logger.debug("= {}", res.toStringLong());
				
			}
		}
		
		logger.debug("perf: biggest CPT was {} with order {}", biggestCPT, orderOtherVariables);
		/*if (logger.isDebugEnabled()) {
			logger.debug(res.toStringLong());
		}*/
		
		return res;
	}

	/**
	 * If we want to know the probabilities for variable X,
	 * eliminates all the parent variables from the BN 
	 * @param n
	 * @return
	 */
	protected Factor getFactorByEliminationFor(NodeCategorical n) {

		Factor f = null;
		List<Factor> factorsToProcessL = n.getAllAncestors().stream().map(m -> bn.getFactor(m)).collect(Collectors.toList());
		LinkedHashSet<Factor> factorsToProcess = new LinkedHashSet<>(
														bn.enumerateNodes()
															.stream()
															.map(m -> bn.getFactor(m))
															.collect(Collectors.toList())
															);
		factorsToProcess.retainAll(factorsToProcessL);

		Set<NodeCategorical> toSum = new HashSet<>(bn.getNodes().size());
		
		for (Factor ff : factorsToProcess) {
			
			logger.debug("eliminating variable {}", ff);
			
			// eliminate f
			if (f == null)
				f = ff;
			else {
				logger.debug("multiply {} by {}", f, ff);
				toSum.addAll(f.variables);
				f = f.multiply(ff);
				for (NodeCategorical varToSum: toSum) {
					logger.debug("summing {} in {}", varToSum.name, f);
					f = f.sumOut(varToSum);
				}
				toSum.clear();
			}
		}
		
		// last summing
		toSum.addAll(f.variables);
		toSum.remove(n);
		for (NodeCategorical varToSum: toSum) {
			logger.debug("summing {} in {}", varToSum.name, f);
			f = f.sumOut(varToSum);
		}
		
		logger.debug("so factor is {}", f);
		return f;
	}
	
	

	@Override
	protected double retrieveConditionalProbability(NodeCategorical n, String s) {

		//if (2==2)
		//	return eliminationAsk(n).get(n.name, s);
		
		// if there is no evidence, just return the prior probability !
		if (evidenceVariable2value.isEmpty()) {
			return n.getConditionalProbabilityPosterior(s);
		}
		
		try {
		
			logger.trace("computing conditional probability p({}={}|evidence)", n.name, s);
			{
				String valueFromEvidence = evidenceVariable2value.get(n);
				if (valueFromEvidence != null) {
					if (valueFromEvidence.equals(s))
						return 1.;
					else
						return 0.;
					
				}
			}
			
			Set<NodeCategorical> set = new HashSet<>(1);
			set.add(n);
			Factor f = computeFactorPosteriorMarginals(set);
			
			 // TODO optimser avvess a une facteur avec 1 var
			f.normalize();
			if (f.variables.isEmpty()) {
				logger.warn("going to fail here for {}={}?", n, s);
			}
			return f.get(n.name,s);
		
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			logger.warn("got exception while computing, checking if Pr(evidence)=0?");
			if (getProbabilityEvidence()==0) {
				throw new IllegalArgumentException("Pr(evidence)=0 with evidence="+evidenceVariable2value+"; impossible to compute posterior probabilities");
			} else {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	protected double[] retrieveConditionalProbability(NodeCategorical n) {
		
		if (evidenceVariable2value.containsKey(n))
			return getEvidenceAsDoubleArray(n);
		
		Set<NodeCategorical> set = new HashSet<>(1);
		set.add(n);
		Factor f = computeFactorPosteriorMarginals(set);
		f.normalize();
	
		// TODO optimiser access à facteur avec une variable
		double[] res = new double[n.getDomainSize()];
		for (int i=0; i<n.getDomainSize(); i++) {
			res[i] = f.get(n.name, n.getValueIndexed(i));
		}
		return res;
	
		//return n.getDomain().stream().collect(Collectors.toMap(s->s, s->f.get(n.name,s)));
	}


	@Override
	public void compute() {
		
		eliminationOrderForEvidence = null;
		factorsForEvidence = null;
		
		cacheNode2factorForEvidence.clear();
		
		/*eliminationOrder = bn.enumerateNodes()
				.stream()
				.filter(n -> !evidenceVariable2value.containsKey(n))
				.collect(Collectors.toList())
				;
		node2factorForEvidence = eliminationOrder.stream().collect(Collectors.toMap(
				m->m, 
				m->bn.getFactor(m).reduction(evidenceVariable2value)
				));
		*/
		
		// call parent to tag clean
		super.compute();
		
	}
	

	@Override
	protected double computeProbabilityEvidence() {
		
		// easy solution if evidence is empty
		if (evidenceVariable2value.isEmpty())
			return 1.0;
		
		Factor f = computeFactorPosteriorMarginals(Collections.emptySet());
				
		return f.getUniqueValue();
	}
	
	private List<NodeCategorical> getEliminationOrderForEvidence() {
		if (eliminationOrderForEvidence == null)
			eliminationOrderForEvidence = bn.enumerateNodes()
					.stream()
					.filter(n -> !evidenceVariable2value.containsKey(n))
					.collect(Collectors.toList())
					; // getEliminationOrderOptimalForZero(bn.getNodes());
		return eliminationOrderForEvidence;
	}
	
	private Map<NodeCategorical,Factor> getFactorsForEvidence() {
		
		if (factorsForEvidence == null) {
			// first remove the useless dimensions in the factors
			// bn.getNodes()
			factorsForEvidence = getEliminationOrderForEvidence().stream().collect(Collectors.toMap(
					m->m, 
					m->bn.getFactor(m).reduction(evidenceVariable2value)
					));
			
			/*
			// now we should integrate into each factor the corresponding children related to evidence...
			// start from evidence
			Set<NodeCategorical> processedAlready = new HashSet<>();
			List<NodeCategorical> affectedByEvidence = new LinkedList<>(evidenceVariable2value.keySet());
			
			for (NodeCategorical nWithEvidence: evidenceVariable2value.keySet()) {
				
				Factor fn = nWithEvidence.asFactor().reduction(evidenceVariable2value);
				
				for (NodeCategorical pWithEvidence: nWithEvidence.getParents()) {
					
					// skip the parent if it is constrained by evidence already
					if (evidenceVariable2value.containsKey(pWithEvidence))
						continue;
					
					// the factor for this parent should be modified by our evidence 
					Factor fp = factorsForEvidence.get(pWithEvidence);
					
					fp = fn.multiply(fp);
					
					factorsForEvidence.put(pWithEvidence, fp);
				}
			}
			
			// then normalize
			for (NodeCategorical n: factorsForEvidence.keySet()) {
				factorsForEvidence.get(n).normalize();
			}*/
		}
		return factorsForEvidence;
	}
	
	/*
	 * Generates an instanciation of the network given current evidence. 
	 * Manipulates factors to be more efficient
	 * TODO buged now!
	 * @return
	 */
	//@Override
	public final Map<NodeCategorical,String> sampleOneTODO() {
		
		// TODO just in case of error elsewhere
		// double probabilityEvidence = getProbabilityEvidence();
		// if (probabilityEvidence == 0.)
		// throw new IllegalArgumentException("evidence has probabity 0; cannot sample");
		
		// ref avant optim sur gerland 1 pour 1000 individus:
		// multiplication: 2859043, additions:2721293
		// multiplication: 2836213, additions:2698641

		// after computing all the values per domain at once
		// multiplication: 1986842, additions:1856842
		
		// after using reduction of factors:
		// multiplication: 0, additions:0, cache hits:0 and miss:0


		// the future result
		Map<NodeCategorical,String> node2attribute = new HashMap<>(bn.getNodes().size());
		node2attribute.putAll(evidenceVariable2value);

		// we iterate variables in our natural order 
		List<NodeCategorical> eliminationOrder = getEliminationOrderForEvidence();
					/*bn.enumerateNodes()
														.stream()
														.filter(n -> !evidenceVariable2value.containsKey(n))
														.collect(Collectors.toList())
														; // getEliminationOrderOptimalForZero(bn.getNodes());
					*/
		// at the beginning, our CPTS are reduced already by the initial evidence 
		// so later we will just create evidence in order !
		Map<NodeCategorical,Factor> node2factor = new HashMap<>(getFactorsForEvidence());
		/*eliminationOrder.stream().collect(Collectors.toMap(
				m->m, 
				m->bn.getFactor(m).reduction(evidenceVariable2value)
				));
		*/
		
		
		//LinkedList<NodeCategorical> reverse = new LinkedList<>(bn.enumerateNodes());
		//Collections.reverse(reverse);
		
		for (NodeCategorical n: eliminationOrder) {
			
			// get the factor for this variable
			Factor f = node2factor.get(n);
			
			// in theory, this factor should not have any parent, as it was reduced already :-)
			if (f.variables.size() > 1)
				throw new RuntimeException("wrong iteration order... factor "+f+" for variable "+n+" should not have more than one variable anymore !");
			
			// pick up a value
			String value = null;

			final double random = GenstarRandom.getInstance().nextDouble();
			// TODO ??? n.getParentsCardinality() * 
			double cumulated = 0.;				
			for (String v: n.getDomain()) {
				cumulated += f.get(n.name, v); // TODO optimiser acces une variable
				if (cumulated >= random) {
					value = v;
					break;
				}
			}
			
			if (value == null)
				throw new RuntimeException("oops, should have picked a value based on postererior probabilities, but they sum to "+cumulated);
			

			// that' the property of this individual
			node2attribute.put(n, value);
			
			// interestingly, we are sure of this value. 
			// so we can reduce all further factors
			// note its useless to call it for the first ones which do not refer to it

			
			for (int i=eliminationOrder.indexOf(n)+1; i<eliminationOrder.size(); i++) {
				NodeCategorical nx = eliminationOrder.get(i);
				Factor fx = node2factor.get(nx);
				
				logger.trace("reducing factor {} knowing {}={}", f, n, value);
				
				node2factor.put(nx, fx.reduction(n, value));
									
			}
			
		}
			
		// reset evidence to its original value
		
		return node2attribute;
	}

}
