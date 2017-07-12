package gospl.algo.sr.bn;

import java.util.Map;

/**
 * This inference engine select the best inference engine provided the question asked
 * 
 * @author Samuel Thiriot
 *
 */
public final class BestInferenceEngine extends AbstractInferenceEngine {


	private EliminationInferenceEngine eliminationInferenceEngine = null;
	private SimpleConditionningInferenceEngine simpleConditionningInferenceEngine = null;
	private RecursiveConditionningEngine recursiveConditionningEngine = null;

	public BestInferenceEngine(CategoricalBayesianNetwork bn) {
		super(bn);
	}


	private RecursiveConditionningEngine getRecursiveConditionningEngine() {
		if (recursiveConditionningEngine == null) {
			recursiveConditionningEngine = new RecursiveConditionningEngine(bn);
			recursiveConditionningEngine.compute();
		}
		return recursiveConditionningEngine;
	}
	private EliminationInferenceEngine getEliminationInferenceEngine() {
		if (eliminationInferenceEngine == null) {
			eliminationInferenceEngine = new EliminationInferenceEngine(bn);
			eliminationInferenceEngine.compute();
		}
		return eliminationInferenceEngine;
	}
	
	private SimpleConditionningInferenceEngine getSimpleConditionningInferenceEngine() {
		if (simpleConditionningInferenceEngine == null) {
			simpleConditionningInferenceEngine = new SimpleConditionningInferenceEngine(bn);
			simpleConditionningInferenceEngine.compute();
		}
		return simpleConditionningInferenceEngine;
	}
	

	public void compute() {
		
		if (simpleConditionningInferenceEngine != null)
			simpleConditionningInferenceEngine.compute();

		if (eliminationInferenceEngine != null)
			eliminationInferenceEngine.compute();
		
		if (recursiveConditionningEngine != null)
			recursiveConditionningEngine.compute();
		
		
		super.compute();
		
	}
	
	@Override
	protected double retrieveConditionalProbability(NodeCategorical n, String s) {
		
		AbstractInferenceEngine ie = null;

		// if there is no evidence and the node is more at the root, maybe its better to use simple conditionning ?
		// TODO 
		
		// if there is no evidence, 
		
		// if there is evidence... 
		
		
		// TODO Auto-generated method stub
		ie = getRecursiveConditionningEngine();
		if (!ie.evidenceVariable2value.equals(evidenceVariable2value)) {
			ie.clearEvidence();
			ie.addEvidence(evidenceVariable2value);
			ie.compute();
		}
		
		return ie.retrieveConditionalProbability(n, s);
	}

	@Override
	protected double[] retrieveConditionalProbability(NodeCategorical n) {
		
		AbstractInferenceEngine ie = null;

		
		// to retrieve all the values for one variable, variable elimination sounds more efficient, no? 
		ie = getRecursiveConditionningEngine();
		
		if (!ie.evidenceVariable2value.equals(evidenceVariable2value)) {
			ie.clearEvidence();
			ie.addEvidence(evidenceVariable2value);
			ie.compute();
		}
		
		return ie.retrieveConditionalProbability(n);
		
	}

	@Override
	protected double computeProbabilityEvidence() {
		
		AbstractInferenceEngine ie = null;
		
		// if there is not evidence, we should have returned 1 already (parent)
		
		// if evidence is on the first nodes of the network, then simple conditionning should be efficient
		// TODO
		
		// in general, recursive conditionning is the best solution
		// (because it's going to compute only the variables required for evidence)
		ie = getEliminationInferenceEngine();
		
		if (!ie.evidenceVariable2value.equals(evidenceVariable2value)) {
			ie.clearEvidence();
			ie.addEvidence(evidenceVariable2value);
			ie.compute();
		}
		
		return ie.computeProbabilityEvidence();
	}

	/**
	 * Generates an instanciation of the network given current evidence. 
	 * The default implementation works for any inference engine, but inheriting classes
	 * might define more efficient methods.
	 * @return
	 */
	public Map<NodeCategorical,String> sampleOne() {
		
		// sample without evidence: the best solution is always simple conditionning !
		if (evidenceVariable2value.isEmpty())
			getSimpleConditionningInferenceEngine().sampleOne();
			
		AbstractInferenceEngine ie = null;
		
		// sample with evidence: the best solution is to use elimination inference
		ie = getEliminationInferenceEngine();
				
		// TODO reset !!!
		
		if (!ie.evidenceVariable2value.equals(evidenceVariable2value)) {
			ie.clearEvidence();
			ie.addEvidence(evidenceVariable2value);
			ie.compute();
		}
		
		return ie.sampleOne();
			
	}
	
	
}
