package gospl.algo.bn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import gospl.algo.bn.BayesianNetwork;
import gospl.algo.bn.NodeCategorical;
import gospl.algo.bn.SimpleConditionningInferenceEngine;

public class TestSimpleConditionningInference {


	@Test
	public void testSimpleInference() {

		BayesianNetwork bn = new BayesianNetwork("test1");
		
		NodeCategorical nGender = new NodeCategorical("gender");
		nGender.addDomain("male", "female");
		nGender.setProbabilities(0.55, "male");
		nGender.setProbabilities(0.45, "female");
		
		NodeCategorical nAge = new NodeCategorical("age");
		nAge.addParent(nGender);
		nAge.addDomain("<15", ">=15");
		nAge.setProbabilities(0.55, "<15", "gender", "male");
		nAge.setProbabilities(0.45, ">=15", "gender", "male");
		nAge.setProbabilities(0.50, "<15", "gender", "female");
		nAge.setProbabilities(0.50, ">=15", "gender", "female");

		bn.add(nGender);
		bn.add(nAge);
		
		SimpleConditionningInferenceEngine ie = new SimpleConditionningInferenceEngine(bn);
		
		// test probabilities with no evidence
		assertEquals(0.55, ie.getConditionalProbability(nGender, "male").doubleValue(), 1e-5);
		assertEquals(0.45, ie.getConditionalProbability(nGender, "female").doubleValue(), 1e-5);
		assertEquals(0.5275d, ie.getConditionalProbability(nAge, "<15").doubleValue(), 1e-5);
		assertEquals(0.4725d, ie.getConditionalProbability(nAge, ">=15").doubleValue(), 1e-5);

		// test with evidence
		ie.addEvidence(nGender, "male");
		assertEquals(1.0d, ie.getConditionalProbability(nGender, "male").doubleValue(), 1e-5);
		assertEquals(0.0d, ie.getConditionalProbability(nGender, "female").doubleValue(), 1e-5);
		assertEquals(0.55d, ie.getConditionalProbability(nAge, "<15").doubleValue(), 1e-5);
		assertEquals(0.45d, ie.getConditionalProbability(nAge, ">=15").doubleValue(), 1e-5);
		
		// or another one
		ie.addEvidence(nGender, "female");
		assertEquals(0.5d, ie.getConditionalProbability(nAge, "<15").doubleValue(), 1e-5);
		assertEquals(0.5d, ie.getConditionalProbability(nAge, ">=15").doubleValue(), 1e-5);
		
		// we should be back to no evidence
		ie.clearEvidence();
		assertEquals(0.5275d, ie.getConditionalProbability(nAge, "<15").doubleValue(), 1e-5);
		assertEquals(0.4725d, ie.getConditionalProbability(nAge, ">=15").doubleValue(), 1e-5);

		// let's try to add evidence in a node which is no root
		ie.addEvidence(nAge, "<15");
		// this one is now known for sure
		assertEquals(1.0d, ie.getConditionalProbability(nAge, "<15").doubleValue(), 1e-5);
		assertEquals(0.0d, ie.getConditionalProbability(nAge, ">=15").doubleValue(), 1e-5);

		// but we cannot know anything about this one ! It should fail, as the engine is simply not able to compute it
		try {
			ie.getConditionalProbability(nGender, "male");
			fail("we should not be able to compute this with simple conditionning");
		} catch (IllegalArgumentException e) {
		}

	}

}
