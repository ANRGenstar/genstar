package gospl.algo.sr.bn;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class EliminationInferenceEngine extends AbstractInferenceEngine {

	private Logger logger = LogManager.getLogger();
	
	public EliminationInferenceEngine(CategoricalBayesianNetwork bn) {
		super(bn);
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


	@Override
	public Factor computeFactorPriorMarginals(Set<NodeCategorical> variables) {
		
		// TODO efficient order !
		LinkedHashSet<NodeCategorical> remainingVariables = new LinkedHashSet<>(bn.enumerateNodes());
		remainingVariables.removeAll(variables);
		
		// optimisation: only focus on relevant variables
		remainingVariables.retainAll(selectRelevantVariables(variables, evidenceVariable2value, bn.getNodes()));
		
		List<NodeCategorical> sorted = new ArrayList<>(remainingVariables);

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
		
		// Collections.shuffle(sorted);
				
		return computeFactorPriorMarginals(variables, sorted);
		
	}
	
	/**
	 * Computes prior (that is, without evidence) probabilities in the form of a factor. 
	 * Uses CPT as factors and elimination. 
	 * @param variables
	 * @param orderOtherVariables
	 * @return
	 */
	protected Factor computeFactorPriorMarginals(Set<NodeCategorical> variables, List<NodeCategorical> orderOtherVariables) {
		
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
					logger.debug("mult {} X {}", f, f2);
					f = f.multiply(f2);
				}
				node2factor.remove(m);
			}
			
			biggestCPT = Math.max(biggestCPT, f.values.size());
			
			// sum of all relevant factors
			logger.debug("sum {} for {}", n, f);
			f = f.sumOut(n);
			
			// now replace fk by fi
			node2factor.put(n, f);
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
		
		
		Factor res = null;
		for (Factor f: node2factor.values()) {
			if (res == null)
				res = f;
			else {
				//logger.debug(res.toStringLong());

				logger.debug("mult {} X {}", res, f);
				res = res.multiply(f);
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

		Set<NodeCategorical> set = new HashSet<>(1);
		set.add(n);
		Factor f = computeFactorPriorMarginals(set);
		 // TODO optimser avvess a une facteur avec 1 var
		f.normalize();
		return f.get(n.name,s);
	}

	@Override
	protected double[] retrieveConditionalProbability(NodeCategorical n) {
		
		Set<NodeCategorical> set = new HashSet<>(1);
		set.add(n);
		Factor f = computeFactorPriorMarginals(set);
		f.normalize();
		// TODO optimiser access à facteur avec une variable
		double[] res = new double[n.getDomainSize()];
		for (int i=0; i<n.getDomainSize(); i++) {
			res[i] = f.get(n.name, n.getValueIndexed(i));
		}
		return res;
		//return n.getDomain().stream().collect(Collectors.toMap(s->s, s->f.get(n.name,s)));
	}

}
