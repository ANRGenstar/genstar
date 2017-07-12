package gospl.algo.sr.bn;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.util.random.GenstarRandom;

public class RecursiveConditionningEngine extends AbstractInferenceEngine {

	private Logger logger = LogManager.getLogger();

	protected DNode dtreeWithoutEvidence = null;
	private List<NodeCategorical> eliminationOrder = null;
	
	protected DNode dtreeWithEvidence = null;
	private List<NodeCategorical> eliminationOrderWithEvidence = null;

	
	/**
	 * normalizing factor for this evidence
	 */
	protected Double norm = null;

	
	public RecursiveConditionningEngine(CategoricalBayesianNetwork bn) {
		super(bn);
	}
	
	/**
	 * Includes evidence inside the dtree.
	 * Futher calls will be quicker 
	 */
	public void internalizeEvidence() {
		// TODO ???
	}

	@Override
	public void compute() {
		
		// when the evidence changed, we might:
		// - either instanciate the tree on evidence; in this case, we create a novel dtree every time evidence changed, but we go quicker to compute it
		// - or we keep the same tree, and just query it; less work if we are just queried once, but each query will be slower
		
		// built the dtree for this network
		//dtree = cacheEvidence2dtree.get(evidenceVariable2value);
		//dtree = cacheEvidenceToDTree.get(evidenceVariable2value);
		
		// this one is not valid anymore as the evidence changed
		dtreeWithEvidence = null;
		dtreeWithoutEvidence = null;
		norm = null;
		
		if (dtreeWithoutEvidence == null) {
			
			/*
			// should build this dtree
			
			eliminationOrder = EliminationOrderBestFirstSearch.computeEliminationOrder(bn);
			//eliminationOrder = EliminationOrderDeepFirstSearch.computeEliminationOrder(bn);
			
			//Hypergraph hg = new Hypergraph(bn);
			//logger.info("hypergraph is:\n{}",hg.getDetailedRepresentation());
			
			logger.debug("building the generic dtree without evidence...");
			dtreeWithoutEvidence = DNode.eliminationOrder2DTree(bn, eliminationOrder);
			logger.info("created dtree:\n{}", dtreeWithoutEvidence);
			dtreeWithoutEvidence.exportAsGraphviz(new File("/tmp/dtree.dot"));
			//logger.debug("eliminating in the dtree the variables {}...", evidenceVariable2value);
			//dtree.instanciate(evidenceVariable2value);
			//cacheEvidenceToDTree.put(evidenceVariable2value, dtree);
			
			*/
			
		} else {
			//logger.debug("retrieve dtree from cache for evidence {}", evidenceVariable2value);
			//norm = cacheEvidence2norm.get(evidenceVariable2value);
		}
		
		// yet we recompute the norm (p evidence) at any evidence change

		// compute the evidence proba
		if (evidenceVariable2value.isEmpty())
			norm = 1.;
		else {
			
			/*
			norm = cacheEvidenceToNorm.get(evidenceVariable2value);
			if (norm == null) {
				logger.info("computing p(evidence)=p{{}}=?...",evidenceVariable2value);
				norm = dtreeWithoutEvidence.recursiveConditionning(evidenceVariable2value);
				cacheEvidenceToNorm.put(new HashMap<>(evidenceVariable2value), norm);
				logger.info("computed p(evidence)=p{{}}={}...",evidenceVariable2value,norm);

				InferencePerformanceUtils.singleton.incCacheMiss();
			} else {
				logger.info("retrieve p(evidence) from cache: {}", norm);
				InferencePerformanceUtils.singleton.incCacheHit();
			}
			*/
		}
		
		logger.debug("dtree is:\n {}", dtreeWithoutEvidence);
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
	
		return getDtreeWithEvidence().recursiveConditionning(n2v) * getProbabilityEvidence();
		
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
				p = getDtreeWithEvidence().recursiveConditionning(n2v) / getProbabilityEvidence();
			}
			res[i] = p;
			total += p;
		}
		
		res[n.getDomainSize()-1] = 1.0 - total;
		
		return res;
		
	}

	@Override
	protected double computeProbabilityEvidence() {
		
		
		if (norm == null)
			norm = getDtreeWithoutEvidence().recursiveConditionning(evidenceVariable2value);
		 
		
		return norm;
	
	}
	
	private DNode getDtreeWithoutEvidence() {

		if (this.dtreeWithoutEvidence == null) {

			eliminationOrder = EliminationOrderBestFirstSearch.computeEliminationOrder(bn);
			//eliminationOrder = EliminationOrderDeepFirstSearch.computeEliminationOrder(bn);
			
			//Hypergraph hg = new Hypergraph(bn);
			//logger.info("hypergraph is:\n{}",hg.getDetailedRepresentation());
			
			logger.debug("building the generic dtree without evidence...");
			dtreeWithoutEvidence = DNode.eliminationOrder2DTree(bn, eliminationOrder);
			logger.info("created dtree:\n{}", dtreeWithoutEvidence);
			dtreeWithoutEvidence.exportAsGraphviz(new File("/tmp/dtree.dot"));
			//logger.debug("eliminating in the dtree the variables {}...", evidenceVariable2value);
			//dtree.instanciate(evidenceVariable2value);
			//cacheEvidenceToDTree.put(evidenceVariable2value, dtree);
			
			
		}
		
		return this.dtreeWithoutEvidence;
		
	}
	
	private DNode getDtreeWithEvidence() {
				
		
		if (this.dtreeWithEvidence == null) {
			logger.debug("creating dtree for evidence {}", evidenceVariable2value);

			// construct the elimination order based on evidence 
			/*this.eliminationOrderWithEvidence = EliminationOrderDeepFirstSearch.computeEliminationOrder(
					bn, 
					bn.getNodes().stream().filter(v -> !evidenceVariable2value.containsKey(v)).collect(Collectors.toSet())
					);
			*/
			this.eliminationOrderWithEvidence = EliminationOrderBestFirstSearch.computeEliminationOrder(
					bn, 
					bn.getNodes().stream().filter(v -> !evidenceVariable2value.containsKey(v)).collect(Collectors.toSet())
					);
			
			
			this.dtreeWithEvidence = DNode.eliminationOrder2DTree(bn, eliminationOrderWithEvidence);
			this.dtreeWithEvidence.exportAsGraphviz(new File("/tmp/dtree_evidence.dot"));

			logger.info("generated dtree with evidence {}:\n{}", evidenceVariable2value, dtreeWithEvidence);
		
		}
		
		return this.dtreeWithEvidence;
	}
	
	// without cache 
	//multiplication: 29930028, additions:29930028, cache hits:61658255 and miss:5130967
	// with cache var U var U acuteset
	// multiplication: 12385379, additions:12385379, cache hits:2700904 and miss:16829045
	// with cache var U var inter acteset
	// WRONG RES
	// with cache vars U
	// multiplication: 707422, additions:707422, cache hits:12640077 and miss:2825249

	// without cache: 
	
	/*
	 * In the case of a dtree, its better to explore evidence in the order of the dtree ! 
	 */
	@Override
	public Map<NodeCategorical,String> sampleOne() {
		
		if (dirty)
			compute();
		
		logger.trace("sampling for evidence {}", evidenceVariable2value);
		// TODO only in case of error ? 
		// if (getProbabilityEvidence() == 0.)
		// 	throw new IllegalArgumentException("evidence asserted ("+evidenceVariable2value+") is invalid: Pr(evidence)=0.");
		// TODO check it still !

		// start from our dtree with evidence
		DNode dtreeWithEvidence = getDtreeWithEvidence();
		
		/*
		if (evidenceVariable2value.isEmpty()) {
			dtreeWithEvidence = dtreeWithoutEvidence;
		} else {
			dtreeWithEvidence = getDtreeWithEvidence(); //.clone();
		}
		*/
		
		// the difficulty here is:
		// - we know we can compute root nodes with no problem, as they are not dependant to evidence. Their computation should be straightforward.
		// - yet our structure is precisely supposed to help us to answer quickly queries, not depending to the order of BN. Especially as it was computed based on evidence !
		
		// in the order of the Bayesian network, let's explore the nodes
		Map<NodeCategorical,String> sampled = new HashMap<>(bn.getNodes().size());
		sampled.putAll(evidenceVariable2value);
		logger.trace("from evidence, we know: {}", sampled);
		double normEvidence = 1.0;
		
		Map<NodeCategorical,String> n2v = new HashMap<>(bn.getNodes().size());
		
				
		for (NodeCategorical n: bn.enumerateNodes()) {
		
			// skip nodes for which value is known
			if (evidenceVariable2value.containsKey(n))
				continue;
		
			// let's try to have a value for this one 
			logger.trace("selecting a value for {}=?", n);
			
			// pick up a value
			String value = null;

			final double random = GenstarRandom.getInstance().nextDouble() * normEvidence;
			double cumulated = 0.;				
			for (String v: n.getDomain()) {
				
				n2v.put(n, v);
				// TODO if no evidence, we should use prior instead (quicker !)
				double thep = dtreeWithEvidence.recursiveConditionning(n2v);
				logger.trace("p({}={}|{})={}", n, v, n2v, thep);
				cumulated += thep;
				// TODO optimiser acces une variable
				if (cumulated >= random) {
					value = v;
					break;
				}
			}

			logger.trace("selected {}={}", n,value);

			if (value == null)
				throw new RuntimeException("oops, should have picked a value based on postererior probabilities, but they sum to "+cumulated);
		
			// that' the property of this individual
			sampled.put(n, value);
			
			logger.trace("asserting evidence {}", n2v);

			// TODO perf: we might only send this last value
			//dtreeWithEvidence.instanciate(n2v);
			normEvidence = 	dtreeWithEvidence.recursiveConditionning(n2v);

			
		}
	
		return sampled;
	}
	
}
