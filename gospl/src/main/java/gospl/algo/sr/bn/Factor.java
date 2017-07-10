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
 * @param <N>
 */
public class Factor<N extends FiniteNode<N>> {

	private final BayesianNetwork<N> bn;
	private final Set<N> variables;
	
	private Map<Map<N,String>,BigDecimal> values = new HashMap<>();
	
	/**
	 * Creates a factor over these variables
	 * @param bn
	 * @param variables
	 */
	public Factor(BayesianNetwork<N> bn, Set<N> variables) {
		this.bn = bn;
		this.variables = variables;
	}
	
	public void setFactor(Map<N,String> instanciations, BigDecimal p) {
		values.put(instanciations, p);	
	}
	
	/**
	 * Gets, or computes, the value of the factor for a given set of instantiations (values for variables)
	 * @param instantiations
	 * @return
	 */
	public BigDecimal get(Map<N,String> instantiations) {
		
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
	
	public Factor<N> sumOut(N var) {
		Set<N> novelSet = new HashSet<>(variables);
		novelSet.remove(var);
		Factor<N> res = new Factor<>(bn, novelSet);
		
		// TODO
		Set<Map<N,String>> done = new HashSet<>(values.size()/2);
		for (Map<N,String> instanciation: values.keySet()) {
			BigDecimal p1 = values.get(instanciation);
			
		}
		
		return res;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("factor(");
		for (N n: variables) {
			if (sb.length() > "factor(".length()) {
				sb.append(",");
			}
			sb.append(n.name);
		}
		sb.append(")");
		return sb.toString();
	}

}
