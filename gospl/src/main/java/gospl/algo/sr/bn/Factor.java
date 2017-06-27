package gospl.algo.sr.bn;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
	
	protected Map<Map<NodeCategorical,String>,BigDecimal> values = new HashMap<>();
	
	
	public static Factor createFromCPT(CategoricalBayesianNetwork bn, NodeCategorical var) {
		
		Set<NodeCategorical> variables = new HashSet<>(var.getParents());
		variables.add(var);
		
		Factor f = new Factor(bn, variables);
		
		for (IteratorCategoricalVariables it = bn.iterateDomains(var.getParents()); it.hasNext(); ) {
			Map<NodeCategorical,String> v2n = it.next();
			for (String v: var.getDomain()) {
				BigDecimal d = var.getProbability(v, v2n);
				HashMap<NodeCategorical,String> v2n2 = new HashMap<>(v2n);
				v2n2.put(var, v);
				f.setFactor(v2n2, d);
			}
		}
		return f;
	}
	
	/**
	 * Creates a factor over these variables
	 * @param bn
	 * @param variables
	 */
	public Factor(CategoricalBayesianNetwork bn, Set<NodeCategorical> variables) {
		this.bn = bn;
		this.variables = variables;
	}
	
	public void setFactor(Map<NodeCategorical,String> instanciations, BigDecimal p) {
		values.put(instanciations, p);	
	}
	
	/**
	 * Gets, or computes, the value of the factor for a given set of instantiations (values for variables)
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
		
		// compute on demand
		p = bn.jointProbability(instantiations, Collections.emptyMap());
		values.put(instantiations, p);
		
		return p;
	}
	
	public BigDecimal get(String... sss) {
		if (sss.length != variables.size()*2)
			throw new IllegalArgumentException("invalid keys and values");
		Map<NodeCategorical,String> n2s = new HashMap<>(variables.size());
		for (int i=0; i<sss.length; i+=2) {
			NodeCategorical n = bn.getVariable(sss[i]);
			if (n == null || !variables.contains(n))
				throw new IllegalArgumentException("Unknown variable "+sss[i]);
			String v = sss[i+1];
			if (!n.getDomain().contains(v))
				throw new IllegalArgumentException("unknown value "+v+" for variable "+sss[i]);
			n2s.put(n, v);
		}
		return this.get(n2s);
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
				res.setFactor(it2m, times);
			}
		}
		
		return res;
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

}
