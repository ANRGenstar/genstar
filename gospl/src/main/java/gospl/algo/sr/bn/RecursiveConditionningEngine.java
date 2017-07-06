package gospl.algo.sr.bn;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.util.random.GenstarRandom;

public class RecursiveConditionningEngine extends AbstractInferenceEngine {

	private Logger logger = LogManager.getLogger();

	private LRUMap<Map<NodeCategorical,String>,DNode> cacheEvidenceToDTree = new LRUMap<>(1000);

	private LRUMap<Map<NodeCategorical,String>,Double> cacheEvidenceToNorm = new LRUMap<>(1000);

	protected DNode dtreeWithoutEvidence = null;
	
	/**
	 * normalizing factor for this evidence
	 */
	protected Double norm = null;

	
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
		//dtree = cacheEvidenceToDTree.get(evidenceVariable2value);
		
		if (dtreeWithoutEvidence == null) {
			
			// should build this dtree
			
			/*List<NodeCategorical> variables = new ArrayList<>(bn.getNodes());
			Collections.shuffle(variables);
			*/
			
			List<NodeCategorical> variables = EliminationOrderDeepFirstSearch.computeEliminationOrderDeepFirstSearch(bn);
			
			//Hypergraph hg = new Hypergraph(bn);
			//logger.info("hypergraph is:\n{}",hg.getDetailedRepresentation());
			
			logger.debug("building the dtree for evidence {}...", evidenceVariable2value);
			dtreeWithoutEvidence = DNode.eliminationOrder2DTree(bn, variables);
			//logger.debug("eliminating in the dtree the variables {}...", evidenceVariable2value);
			//dtree.instanciate(evidenceVariable2value);
			//cacheEvidenceToDTree.put(evidenceVariable2value, dtree);
			
		} else {
			//logger.debug("retrieve dtree from cache for evidence {}", evidenceVariable2value);
			//norm = cacheEvidence2norm.get(evidenceVariable2value);
		}
		
		// yet we recompute the norm (p evidence) at any evidence change

		// compute the evidence proba
		if (evidenceVariable2value.isEmpty())
			norm = 1.;
		else {
			
			norm = cacheEvidenceToNorm.get(evidenceVariable2value);
			if (norm == null) {
				logger.info("computing p(evidence)...");
				norm = dtreeWithoutEvidence.recursiveConditionning(evidenceVariable2value);
				cacheEvidenceToNorm.put(evidenceVariable2value, norm);
			} else {
				logger.info("retrieve p(evidence) from cache: {}", norm);
			}
			
		}
		
		logger.debug("dtree is: {}", dtreeWithoutEvidence);
		logger.debug("probability for evidence  p({})={}", evidenceVariable2value, norm);

		
		super.compute();
	}
	
	@Override
	protected double retrieveConditionalProbability(NodeCategorical n, String s) {

		{
			String evidenceV = evidenceVariable2value.get(n); 
			if (evidenceV != null) {
				if (evidenceV.equals(s))
					return 1.;
				else 
					return 0.;
			}
		}
		
		Map<NodeCategorical,String> n2v = new HashMap<>(evidenceVariable2value);
		n2v.put(n, s);
		
		double r = dtreeWithoutEvidence.recursiveConditionning(n2v);
		return r / norm;
		
	}

	@Override
	protected double[] retrieveConditionalProbability(NodeCategorical n) {
		
		double[] res = new double[n.getDomainSize()];
		
		double total = 0.;
		
		for (int i=0; i<n.getDomainSize()-1; i++) {
			String v = n.getValueIndexed(i);
			double p;
			if (evidenceVariable2value.containsKey(n)) {
				if (evidenceVariable2value.get(n).equals(v))
					p = 1.;
				else 
					p = 0;
			} else {
				Map<NodeCategorical,String> n2v = new HashMap<>(evidenceVariable2value);
				n2v.put(n, v);
				p = dtreeWithoutEvidence.recursiveConditionning(n2v) / norm;
			}
			res[i] = p;
			total += p;
		}
		
		res[n.getDomainSize()-1] = 1.0 - total;
		
		return res;
		
	}

	@Override
	public double getProbabilityEvidence() {
		if (dirty)
			this.compute();
		
		return norm;
	}
	

	/*
	 * In the case of a dtree, its better to explore evidence in the order of the dtree ! 
	 *
	@Override
	public Map<NodeCategorical,String> sampleOne() {
		
		if (dirty)
			compute();
		
		// create a dtree for this specific evidence 
		// so later the factors will be adapted and efficient.
		
		Map<NodeCategorical,String> modifiedEvidence = new HashMap<>(evidenceVariable2value);
		 
		dtreeWithoutEvidence.generate(modifiedEvidence);
		
		return modifiedEvidence;
	}
	*/
}
