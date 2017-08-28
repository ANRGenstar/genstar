package gospl.algo.sr.bn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.Set;

import org.apache.commons.collections4.map.LRUMap;

/**
 * A factor f over variables X is a function that maps each instantiation 
 * x of variables X to a non-negative number.
 * 
 * TODO optimisation for storage ? use double[] instead of a map ? 
 * 
 * @author Samuel Thiriot
 *
 */
public final class Factor {

	private final CategoricalBayesianNetwork bn;
	protected final Set<NodeCategorical> variables;
	
	protected final boolean keepZeros = false;
	
	protected Map<Map<NodeCategorical,String>,Double> values = new HashMap<>();
	
	protected LRUMap<NodeCategorical,Map<String,Factor>> cacheReductionVariable2ValueToResult = null;
	
	public static int CACHE_LEVEL1 = 50;
	public static int CACHE_LEVEL2 = 100;
	
	
	/**
	 * Creates a factor over these variables
	 * @param bn
	 * @param variables
	 */
	public Factor(CategoricalBayesianNetwork bn, Set<NodeCategorical> variables) {
		this.bn = bn;
		this.variables = variables;
	}
	
	public boolean hasUniqueValue() {
		return variables.isEmpty() && values.size()==1;
	}
	
	/**
	 * Returns the unique value for a factor which as no variable, 
	 * else throws a 
	 * @return
	 */
	public double getUniqueValue() {
		
		if (!variables.isEmpty())
			throw new IllegalArgumentException("Factor "+this+" has more than one variable, cannot return a unique value.");
		
		return values.values().iterator().next();
	}
	
	/**
	 * Clones a factor.
	 */
	public Factor clone() {
		Factor res = new Factor(bn, new HashSet<>(variables));
		for (Map.Entry<Map<NodeCategorical,String>,Double> e: values.entrySet()) {
			res.setFactor(e.getKey(), e.getValue());
		}
		return res;
	}
	
	public double sum() {
		
		// convention
		if (values.isEmpty()) {
			return 1.0;
		}
	
		double total = 0;
		for (Double d: values.values()) {
			total += d;
		}
		return total;
	}
	
	/**
	 * Reduces this factor by removing any reference to nEvidence: keeps only the lines
	 * compliant with nEvidence=vEvidence and removes the other ones. 
	 * after this operation there is one less variables in the factor.
	 * @param nEvidence
	 * @param vEvidence
	 */
	public void reduce(NodeCategorical nEvidence, String vEvidence) {
		
		// quick exit
		if (!variables.contains(nEvidence))
			return;
		
		// for each value in our map...
		
		Map<Map<NodeCategorical,String>,Double> novelvalues = new HashMap<>(values.size()/nEvidence.getDomainSize());
		
		for (Iterator<Map.Entry<Map<NodeCategorical,String>,Double>> itValues = values.entrySet().iterator(); itValues.hasNext();) {
			
			Map.Entry<Map<NodeCategorical,String>,Double> eValues = itValues.next();
			
			Map<NodeCategorical,String> keys = eValues.getKey();
			Double p = eValues.getValue();
			
			String vLine = keys.get(nEvidence);
			if (vLine.equals(vEvidence)) {
				// keep it !
				Map<NodeCategorical,String> n2s = new HashMap<>(keys);
				n2s.remove(nEvidence);
				Double previousp = novelvalues.put(n2s, p);

				if (previousp != null) {
					// oops, we knew this one already. Let's sum instead. 
					novelvalues.put(n2s, p+previousp);
					InferencePerformanceUtils.singleton.incAdditions();
				}
			} else {
				// in fact we want to drop that one 
			}
			
			
		}
		
		variables.remove(nEvidence);
		values = novelvalues;

	}
	
	/**
	 * reduces this factor given evidence, that is replaces values with 0 for each 
	 * combination of values which is not compliant with evidence
	 * 
	 */
	public void reduce(Map<NodeCategorical,String> evidence) {
		
		for (Map.Entry<NodeCategorical,String> e: evidence.entrySet()) {
			reduce(e.getKey(), e.getValue());
			
		}
	
	}
	
	/**
	 * computes a novel factor which is a reduction of this factor. 
	 * @param evidence
	 * @return
	 */
	public Factor reduction(Map<NodeCategorical,String> evidence) {
		// TODO optimization: if the evidence is not related to us, we might return us. 
		//System.err.println("factor before reduction: "+this.values);
		Factor res = this.clone();
		res.reduce(evidence);
		//System.err.println("factor after reduction: "+res.values);
		return res;
	}
	
	public Factor reduction(NodeCategorical n, String s) {
		
		// optimisation: in case we are not concerned by this variable, return this
		if (!variables.contains(n))
			return this;
		
		if (cacheReductionVariable2ValueToResult == null)
			cacheReductionVariable2ValueToResult = new LRUMap<>(Math.min(CACHE_LEVEL1,variables.size()));
		
		Map<String,Factor> cacheForVariable = cacheReductionVariable2ValueToResult.get(n);
		
		if (cacheForVariable == null) {
			cacheForVariable = new LRUMap<>(Math.min(CACHE_LEVEL2, n.getDomainSize()));
			cacheReductionVariable2ValueToResult.put(n, cacheForVariable);
		}
		
		Factor res = cacheForVariable.get(s);
		
		if (res == null) {
			InferencePerformanceUtils.singleton.incCacheMiss();
			res = this.clone();
			res.reduce(n, s);
			cacheForVariable.put(s, res);
		} else {
			InferencePerformanceUtils.singleton.incCacheHit();
		}
		
		return res;
		
		// TODO optimization: if the evidence is not related to us, we might return us. 
		//Factor res = this.clone();
		//res.reduce(n, s);
		//return res;
	}
	
	public void setFactor(Map<NodeCategorical,String> instanciations, double p) {
		if (!keepZeros && p==0)
			values.remove(instanciations);
		else 
			values.put(instanciations, p);	
	}
	
	/**
	 * Gets the value of the factor for a given set of instantiations (values for variables)
	 * @param instantiations
	 * @return
	 */
	public double get(Map<NodeCategorical,String> instantiations) {
		
		// are parameters valid ? 
		if (!variables.containsAll(instantiations.keySet())) {
			throw new IllegalArgumentException("invalid variables "+instantiations.keySet()+" for factor "+this);
		}
		
		double p;
		
		if (instantiations.size() == variables.size()) {
			// perfect solution: just read one single value in this factor 
			
			// get it 
			Double v = values.get(instantiations);
			
			if (v==null & !keepZeros)
				// the fact we red nothing means this is 0.
				p=0.;
			else 
				p = v;
				
		} else {
			throw new IllegalArgumentException("not enough coordinates to get factor data");
			/*
			// seems like we don't have all the values required for reading our index ! 
			// let's sum over it 
			double total = 0.;
			for (IteratorCategoricalVariables itDomain = bn.iterateDomains(variables.stream().filter(v -> !instantiations.containsKey(v)).collect(Collectors.toSet())); itDomain.hasNext(); ) {
				Map<NodeCategorical,String> n2v = itDomain.next();
				n2v.putAll(instantiations);
				Double read = values.get(n2v);
				if (read != null) {
					total += read; // notice we ignore null lines here... for some reason...
					if (total >= 1)
						break;
				}
			}
			p = total;*/
		}
		
		
		// compute on demand
		//p = bn.jointProbability(instantiations, Collections.emptyMap());
		//values.put(instantiations, p);
		
		return p;
	}
	
	public double get(String... sss) {
		return this.get(bn.toNodeAndValue(this.variables, sss));
	}

	
	public Factor sumOut(String varName) {
		return this.sumOut(bn.getVariable(varName));
	}
	
	public Factor sumOut(NodeCategorical var) {
		
		// the novel factor will target all the values but the one we sum
		Set<NodeCategorical> novelSet = new HashSet<>(variables);
		novelSet.remove(var);
		
		Factor res = new Factor(bn, novelSet);
		
		for (IteratorCategoricalVariables it = bn.iterateDomains(novelSet); it.hasNext();) {
			Map<NodeCategorical,String> v2n = it.next();
			double d = 0;
			for (String v: var.getDomain()) {
				v2n.put(var, v);
				d += get(v2n);
				InferencePerformanceUtils.singleton.incAdditions();
			}
			v2n.remove(var);
			res.setFactor(v2n, d);
		}
		
		return res;
	}
	
	public Factor multiply(Factor f) {
		
		if (!bn.getNodes().containsAll(f.variables))
			throw new IllegalArgumentException("the other factor variables do not all belong this network");
		
		// special case of the empty factor: it is invariant, so we can return the other factor 
		if (this.values.isEmpty())
			return f.clone();
		if (f.values.isEmpty())
			return this.clone();
		
		Set<NodeCategorical> vvs = new HashSet<>(this.variables);
		vvs.addAll(f.variables);
		
		Set<NodeCategorical> varsDiff = new HashSet<>(f.variables);
		varsDiff.removeAll(this.variables);
		
		Factor res = new Factor(bn, vvs);
		
		for (IteratorCategoricalVariables it1=bn.iterateDomains(this.variables); it1.hasNext(); ) {
			Map<NodeCategorical,String> it1m = it1.next(); 
			double d1 = this.get(it1m);
			for (IteratorCategoricalVariables it2=bn.iterateDomains(varsDiff); it2.hasNext(); ) {
				Map<NodeCategorical,String> it2m = new HashMap<>(it2.next()); 
				for (NodeCategorical n: this.variables) {
					if (f.variables.contains(n))
						it2m.put(n,it1m.get(n));
				}
				double d2 = f.get(it2m);
				
				it2m.putAll(it1m);
				
				double times = d1 * d2;
				InferencePerformanceUtils.singleton.incMultiplications();
				
				res.setFactor(it2m, times);
			}
		}
		
		return res;
	}
	
	/**
	 * Reduces a factor by suming until only the variables passed as parameter remain.
	 * @param onlyVariables
	 * @return
	 */
	public Factor reduceTo(Set<NodeCategorical> onlyVariables) {
		
		if (variables.equals(onlyVariables))
			return this;
		
		if (!variables.containsAll(onlyVariables))
			throw new IllegalArgumentException("not all of these variables "+onlyVariables+" belong this factor "+this);
		
		Set<NodeCategorical> toRemoveS = new HashSet<>(variables);
		toRemoveS.removeAll(onlyVariables);
		
		List<NodeCategorical> toRemoveL = new ArrayList<>(toRemoveS);
		// TODO optimisation of order 

		Factor f = this;
		for (NodeCategorical toRemove: toRemoveL) {
			f = f.sumOut(toRemove);
		}
		
		return f;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("factor(");
		for (NodeCategorical n: variables) {
			if (sb.length() > "factor(".length()) {
				sb.append(",");
			}
			sb.append(n.name);
		}
		sb.append(")");
		return sb.toString();
	}

	public String toStringLong() {
		StringBuffer sb = new StringBuffer(toString());
		sb.append(":\n");
		for (Map.Entry<Map<NodeCategorical,String>,Double> e: values.entrySet()) {
			sb.append("\t");
			sb.append(e.getKey().entrySet().stream().map(e2->e2.getKey().name+"="+e2.getValue()).collect(Collectors.joining(",")));
			sb.append(":").append(e.getValue()).append("\n");
		}
		return sb.toString();
	}

	/**
	 * Updates the values inside the factor so the total sums to 1
	 */
	public void normalize() {
		
		// sum ?
		double total = 0;
		for (double d: values.values()) {
			total += d;
			InferencePerformanceUtils.singleton.incAdditions();
		}
		
		// do nothing if good already !
		if (total==1)
			return;
		
		// norm !
		for (Entry<Map<NodeCategorical, String>, Double> e: values.entrySet()) {
			values.put(e.getKey(), e.getValue()/total );
			InferencePerformanceUtils.singleton.incMultiplications();
		}
		
	}

	public void divide(int size) {
		// norm !
		for (Entry<Map<NodeCategorical, String>, Double> e: values.entrySet()) {
			values.put(e.getKey(), e.getValue()/size );
			InferencePerformanceUtils.singleton.incMultiplications();
		}
	}

}
