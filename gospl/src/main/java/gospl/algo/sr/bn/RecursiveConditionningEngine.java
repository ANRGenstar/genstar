package gospl.algo.sr.bn;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RecursiveConditionningEngine extends AbstractInferenceEngine {

	private Logger logger = LogManager.getLogger();

	private LRUMap<Map<NodeCategorical,String>,DNode> cacheEvidenceToDTree = new LRUMap<>(50);

	protected DNode dtree = null;
	
	/**
	 * normalizing factor for this evidence
	 */
	protected BigDecimal norm = null;

	
	public RecursiveConditionningEngine(CategoricalBayesianNetwork bn) {
		super(bn);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Includes evidence inside the dtree.
	 * Futher calls will be quicker 
	 */
	public void internalizeEvidence() {
		// TODO
	}

	@Override
	public void compute() {
		
		// when the evidence changed, we might:
		// - either instanciate the tree on evidence; in this case, we create a novel dtree every time evidence changed, but we go quicker to compute it
		// - or we keep the same tree, and just query it; less work if we are just queried once, but each query will be slower
		
		// built the dtree for this network
		//dtree = cacheEvidence2dtree.get(evidenceVariable2value);
		dtree = cacheEvidenceToDTree.get(evidenceVariable2value);
		
		if (dtree == null) {
			
			// should build this dtree
			
			List<NodeCategorical> variables = new ArrayList<>(bn.getNodes());
			Collections.shuffle(variables);
			
			//Hypergraph hg = new Hypergraph(bn);
			//logger.info("hypergraph is:\n{}",hg.getDetailedRepresentation());
			
			logger.debug("building the dtree for evidence {}...", evidenceVariable2value);
			dtree = DNode.eliminationOrder2DTree(bn, variables);
			logger.debug("eliminating in the dtree the variables {}...", evidenceVariable2value);
			dtree.instanciate(evidenceVariable2value);
			cacheEvidenceToDTree.put(evidenceVariable2value, dtree);
			
		} else {
			//logger.debug("retrieve dtree from cache for evidence {}", evidenceVariable2value);
			//norm = cacheEvidence2norm.get(evidenceVariable2value);
		}
		
		// yet we recompute the norm (p evidence) at any evidence change

		// compute the evidence proba
		if (evidenceVariable2value.isEmpty())
			norm = BigDecimal.ONE;
		else {
			logger.debug("computing p(evidence)...");

			norm = dtree.recursiveConditionning(evidenceVariable2value);
		}
		
		logger.debug("dtree is: {}", dtree);
		logger.debug("probability for evidence  p({})={}", evidenceVariable2value, norm);

		
		super.compute();
	}
	
	@Override
	protected BigDecimal retrieveConditionalProbability(NodeCategorical n, String s) {

		if (evidenceVariable2value.containsKey(n)) {
			if (evidenceVariable2value.get(n).equals(s))
				return BigDecimal.ONE;
			else 
				return BigDecimal.ZERO;
		}
		Map<NodeCategorical,String> n2v = new HashMap<>(evidenceVariable2value);
		n2v.put(n, s);
		
		BigDecimal r = dtree.recursiveConditionning(n2v);
		return r.divide(norm, BigDecimal.ROUND_HALF_UP);
		
	}

	@Override
	protected Map<String, BigDecimal> retrieveConditionalProbability(NodeCategorical n) {
		
		Map<String, BigDecimal> res = new HashMap<>();
		
		BigDecimal total = BigDecimal.ZERO;
		
		for (String v: n.getDomain().subList(0, n.getDomainSize()-2)) {
			BigDecimal p = null;
			if (evidenceVariable2value.containsKey(n)) {
				if (evidenceVariable2value.get(n).equals(v))
					p = BigDecimal.ONE;
				else 
					p = BigDecimal.ZERO;
			} else {
				Map<NodeCategorical,String> n2v = new HashMap<>(evidenceVariable2value);
				n2v.put(n, v);
				p = dtree.recursiveConditionning(n2v).divide(norm, BigDecimal.ROUND_HALF_UP);
			}
			res.put(v, p);
			total = total.add(p);
		}
		
		res.put(n.getDomain().get(n.getDomainSize()-1), BigDecimal.ONE.subtract(total));
		
		return res;
		
	}

}
