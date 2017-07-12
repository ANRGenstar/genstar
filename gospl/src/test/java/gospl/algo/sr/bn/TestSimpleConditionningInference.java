package gospl.algo.sr.bn;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestSimpleConditionningInference {


	@Test
	public void testSimpleInference() {

		CategoricalBayesianNetwork bn = new CategoricalBayesianNetwork("test1");
		
		NodeCategorical nGender = new NodeCategorical(bn, "gender");
		nGender.addDomain("male", "female");
		nGender.setProbabilities(0.55, "male");
		nGender.setProbabilities(0.45, "female");
		
		NodeCategorical nAge = new NodeCategorical(bn, "age");
		nAge.addParent(nGender);
		nAge.addDomain("<15", ">=15");
		nAge.setProbabilities(0.55, "<15", "gender", "male");
		nAge.setProbabilities(0.45, ">=15", "gender", "male");
		nAge.setProbabilities(0.50, "<15", "gender", "female");
		nAge.setProbabilities(0.50, ">=15", "gender", "female");
		
		SimpleConditionningInferenceEngine ie = new SimpleConditionningInferenceEngine(bn);
		
		// test probabilities with no evidence
		assertEquals(0.55, ie.getConditionalProbability(nGender, "male"), 1e-5);
		assertEquals(0.45, ie.getConditionalProbability(nGender, "female"), 1e-5);
		assertEquals(0.5275d, ie.getConditionalProbability(nAge, "<15"), 1e-5);
		assertEquals(0.4725d, ie.getConditionalProbability(nAge, ">=15"), 1e-5);

		// test with evidence
		ie.addEvidence(nGender, "male");
		assertEquals(1.0d, ie.getConditionalProbability(nGender, "male"), 1e-5);
		assertEquals(0.0d, ie.getConditionalProbability(nGender, "female"), 1e-5);
		assertEquals(0.55d, ie.getConditionalProbability(nAge, "<15"), 1e-5);
		assertEquals(0.45d, ie.getConditionalProbability(nAge, ">=15"), 1e-5);
		
		// or another one
		ie.addEvidence(nGender, "female");
		assertEquals(0.5d, ie.getConditionalProbability(nAge, "<15"), 1e-5);
		assertEquals(0.5d, ie.getConditionalProbability(nAge, ">=15"), 1e-5);
		
		// we should be back to no evidence
		ie.clearEvidence();
		assertEquals(0.5275d, ie.getConditionalProbability(nAge, "<15"), 1e-5);
		assertEquals(0.4725d, ie.getConditionalProbability(nAge, ">=15"), 1e-5);

		// let's try to add evidence in a node which is no root
		ie.addEvidence(nAge, "<15");
		// this one is now known for sure
		assertEquals(1.0d, ie.getConditionalProbability(nAge, "<15"), 1e-5);
		assertEquals(0.0d, ie.getConditionalProbability(nAge, ">=15"), 1e-5);

		

	}

}
