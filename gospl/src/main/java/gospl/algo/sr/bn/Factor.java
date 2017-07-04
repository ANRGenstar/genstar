package gospl.algo.sr.bn;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A factor f over variables X is a function that maps each instantiation 
 * x of variables X to a non-negative number, denoted f (x).1
 * 
 * @author Samuel Thiriot
 *
 */
public class Factor {

	private final CategoricalBayesianNetwork bn;
	protected final Set<NodeCategorical> variables;
	
	protected final boolean keepZeros = false;
	
	protected Map<Map<NodeCategorical,String>,BigDecimal> values = new HashMap<>();
	
	
	/**
	 * Creates a factor over these variables
	 * @param bn
	 * @param variables
	 */
	public Factor(CategoricalBayesianNetwork bn, Set<NodeCategorical> variables) {
		this.bn = bn;
		this.variables = variables;
	}
	
	/**
	 * Clones a factor.
	 */
	public Factor clone() {
		Factor res = new Factor(bn, new HashSet<>(variables));
		for (Map.Entry<Map<NodeCategorical,String>,BigDecimal> e: values.entrySet()) {
			res.setFactor(e.getKey(), e.getValue());
		}
		return res;
	}
	
	/**
	 * reduces this factor given evidence, that is replaces values with 0 for each 
	 * combination of values which is not compliant with evidence
	 * 
	 */
	public void reduce(Map<NodeCategorical,String> evidence) {
		
		for (Iterator<Map<NodeCategorical,String>> it = values.keySet().iterator(); it.hasNext();) {
			Map<NodeCategorical,String> k = it.next();
			for (NodeCategorical n: k.keySet()) {
				if (!evidence.containsKey(n))
					continue;
				// n is concerned by evidence 
				if (!k.get(n).equals(evidence.get(n))) {
					// and is contradicting evidence
					if (keepZeros)
						values.put(k, BigDecimal.ZERO);
					else 
						it.remove();
					// stop there for this line
					break;
				}
			}
		}
	}
	
	/**
	 * computes a novel factor which is a reduction of this factor. 
	 * @param evidence
	 * @return
	 */
	public Factor reduction(Map<NodeCategorical,String> evidence) {
		Factor res = this.clone();
		res.reduce(evidence);
		return res;
	}
	
	public void setFactor(Map<NodeCategorical,String> instanciations, BigDecimal p) {
		if (!keepZeros && BigDecimal.ZERO.compareTo(p)==0)
			values.remove(instanciations);
		else 
			values.put(instanciations, p);	
	}
	
	/**
	 * Gets the value of the factor for a given set of instantiations (values for variables)
	 * @param instantiations
	 * @return
	 */
	public BigDecimal get(Map<NodeCategorical,String> instantiations) {
		
		// are parameters valid ? 
		if (!variables.equals(instantiations.keySet())) {
			throw new IllegalArgumentException("invalid variables "+instantiations.keySet()+" for factor "+this);
		}
		
		BigDecimal p = values.get(instantiations);
		
		// maybe it's already computed
		if (p != null)
			return p;
		
		if (!keepZeros)
			return BigDecimal.ZERO;
		
		// compute on demand
		//p = bn.jointProbability(instantiations, Collections.emptyMap());
		//values.put(instantiations, p);
		
		return p;
	}
	
	public BigDecimal get(String... sss) {
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
		
		Set<Map<NodeCategorical,String>> done = new HashSet<>(values.size()/2);
		for (IteratorCategoricalVariables it = bn.iterateDomains(novelSet); it.hasNext();) {
			Map<NodeCategorical,String> v2n = it.next();
			BigDecimal d = BigDecimal.ZERO;
			for (String v: var.getDomain()) {
				v2n.put(var, v);
				d = d.add(get(v2n));
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
		
		Set<NodeCategorical> vvs = new HashSet<>(this.variables);
		vvs.addAll(f.variables);
		
		Set<NodeCategorical> varsDiff = new HashSet<>(f.variables);
		varsDiff.removeAll(this.variables);
		
		Factor res = new Factor(bn, vvs);
		
		for (IteratorCategoricalVariables it1=bn.iterateDomains(this.variables); it1.hasNext(); ) {
			Map<NodeCategorical,String> it1m = it1.next(); 
			BigDecimal d1 = this.get(it1m);
			for (IteratorCategoricalVariables it2=bn.iterateDomains(varsDiff); it2.hasNext(); ) {
				Map<NodeCategorical,String> it2m = new HashMap<>(it2.next()); 
				for (NodeCategorical n: this.variables) {
					if (f.variables.contains(n))
						it2m.put(n,it1m.get(n));
				}
				BigDecimal d2 = f.get(it2m);
				
				it2m.putAll(it1m);
				
				BigDecimal times = d1.multiply(d2);
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
		for (Map.Entry<Map<NodeCategorical,String>,BigDecimal> e: values.entrySet()) {
			sb.append(e.getKey()).append(":").append(e.getValue().setScale(8, BigDecimal.ROUND_HALF_UP)).append("\n");
		}
		return sb.toString();
	}

	/**
	 * Updates the values inside the factor so the total sums to 1
	 */
	public void normalize() {
		
		// sum ?
		BigDecimal total = BigDecimal.ZERO;
		for (BigDecimal d: values.values()) {
			total = total.add(d);
			InferencePerformanceUtils.singleton.incAdditions();
		}
		
		// do nothing if good already !
		if (total.compareTo(BigDecimal.ONE)==0)
			return;
		
		// norm !
		for (Entry<Map<NodeCategorical, String>, BigDecimal> e: values.entrySet()) {
			values.put(e.getKey(), e.getValue().divide(total, BigDecimal.ROUND_HALF_UP));
			InferencePerformanceUtils.singleton.incMultiplications();
		}
		
	}

}
