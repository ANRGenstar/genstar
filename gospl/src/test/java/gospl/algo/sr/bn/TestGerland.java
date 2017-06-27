package gospl.algo.sr.bn;

import static gospl.algo.sr.bn.JUnitBigDecimals.assertEqualsBD;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import core.util.random.GenstarRandom;
import gospl.algo.sr.bn.BayesianNetwork;
import gospl.algo.sr.bn.InferencePerformanceUtils;
import gospl.algo.sr.bn.NodeCategorical;
import gospl.algo.sr.bn.SimpleConditionningInferenceEngine;

public class TestGerland {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	protected BayesianNetwork<NodeCategorical> loadGerlandNetwork() {

		File f = new File("./src/test/resources/bayesiannetworks/gerland.xbif");
		
		return BayesianNetwork.loadFromXMLBIF(f);
		
	}
	
	@Test
	public void testLoadGerland() {
		
		BayesianNetwork<NodeCategorical> bn = loadGerlandNetwork();
				
		assertEqualsBD(
				0, 
				bn.getVariable("actif").getProbability("unknown", "age3", "from25to54", "gender", "female"), 
				5
				);
		assertEqualsBD(
				0.922, 
				bn.getVariable("actif").getProbability("oui", "age3", "from25to54", "gender", "female"), 
				5
				);
		assertEqualsBD(
				0.078, 
				bn.getVariable("actif").getProbability("non", "age3", "from25to54", "gender", "female"), 
				5
				);
		
		
	}
	
	@Test
	public void testGenerate() {

		BayesianNetwork<NodeCategorical> bn = loadGerlandNetwork();
		
		SimpleConditionningInferenceEngine ie = new SimpleConditionningInferenceEngine(bn);
		
		for (int i=0; i<100000; i++) {
			Map<NodeCategorical,String> node2attribute = new HashMap<>();
			// define values for each individual
			for (NodeCategorical n: bn.enumerateNodes()) {
				double random = GenstarRandom.getInstance().nextDouble();
				// pick up a value
				BigDecimal cumulated = BigDecimal.ZERO;
				String value = null;
				for (String v : n.getDomain()) {
					cumulated = cumulated.add(ie.getConditionalProbability(n, v));
					if (cumulated.doubleValue() >= random) {
						value = v;
						break;
					}
				}
				if (value == null)
					throw new RuntimeException("oops, should have picked a value!");
				// that' the property of this individual
				node2attribute.put(n, value);
				// store this novel value as evidence for this individual
				ie.addEvidence(n, value);
			}
			// we finished an individual
			// reset evidence
			ie.clearEvidence();
			System.out.println(i+": "+node2attribute);
		}
		
		InferencePerformanceUtils.singleton.display();
		
	}
	

	/**
	 * tests backpropagation with one unique piece of evidence in the graph.
	 */
	@Test
	public void testBackwardsInferenceFromEvidenceOne() {
		
		BayesianNetwork<NodeCategorical> bn = loadGerlandNetwork();
		
		SimpleConditionningInferenceEngine ie = new SimpleConditionningInferenceEngine(bn);
		
		// test 1
		ie.addEvidence("age3", "from15to24");
		
		assertEqualsBD(
				0.3902, 
				ie.getConditionalProbability("age6", "from14to29"),
				4
				);
		
		ie.clearEvidence();
		
		// test 2
		ie.addEvidence("type_salarie", "apprentissage_stage");

		assertEqualsBD(
				0.9646, 
				ie.getConditionalProbability("gender", "male"),
				4
				);
		assertEqualsBD(
				1., 
				ie.getConditionalProbability("salarie", "salarie"),
				4
				);
		
		ie.clearEvidence();
		
		// test 3
		ie.addEvidence("age6", "from90toMore");

		assertEqualsBD(
				.8321, 
				ie.getConditionalProbability("gender", "male"),
				4
				);
		
		InferencePerformanceUtils.singleton.display();

	}
	

	/**
	 * tests backpropagation with two pieces of evidence in the graph.
	 * Both should be taken into account
	 */
	@Test
	public void testBackwardsInferenceFromEvidenceTwo() {
		
		BayesianNetwork<NodeCategorical> bn = loadGerlandNetwork();
		
		SimpleConditionningInferenceEngine ie = new SimpleConditionningInferenceEngine(bn);
		
		ie.addEvidence("actif", "oui");
		ie.addEvidence("type_salarie", "cdd");
		
		// these nodes are directly constrained by evidence
		assertEqualsBD(
				0.5880, 
				ie.getConditionalProbability("gender", "female"),
				4
				);

		assertEqualsBD(
				1., 
				ie.getConditionalProbability("salarie", "salarie"),
				4
				);
		
		assertEqualsBD(
				.6362, 
				ie.getConditionalProbability("age3", "from25to54"),
				4
				);
		
		// these nodes are constrained by known probabilities of their children known
		assertEqualsBD(
				.1995, 
				ie.getConditionalProbability("age6", "from14to29"),
				2 // TODO it's a bit too low... 
				);
		
		// these nodes are not constrained by evidence, so are computed by standard processing
		assertEqualsBD(
				1., 
				ie.getConditionalProbability("type_nonsalarie", "nonpertinent"),
				3
				);
		
		InferencePerformanceUtils.singleton.display();

	}
	
	/**
	 * tests backpropagation with one unique piece of evidence in the graph.
	 */
	@Test
	public void testBackwardsAndForwardsInferenceFromEvidenceOne() {
		
		BayesianNetwork<NodeCategorical> bn = loadGerlandNetwork();
		
		SimpleConditionningInferenceEngine ie = new SimpleConditionningInferenceEngine(bn);
		
		ie.addEvidence("age3", "from15to24");
			
		assertEqualsBD(
				0.3902, 
				ie.getConditionalProbability("age6", "from14to29"),
				4
				);
		
		ie.clearEvidence();
		ie.addEvidence("type_salarie", "apprentissage_stage");

		assertEqualsBD(
				0.9646, 
				ie.getConditionalProbability("gender", "male"),
				4
				);
		assertEqualsBD(
				1., 
				ie.getConditionalProbability("salarie", "salarie"),
				4
				);
		
		ie.clearEvidence();
		ie.addEvidence("age6", "from90toMore");

		// backward
		assertEqualsBD(
				.8321, 
				ie.getConditionalProbability("gender", "male"),
				4
				);
		// forward
		assertEqualsBD(
				.5, 
				ie.getConditionalProbability("age3", "unknown"),
				4
				);
		
		InferencePerformanceUtils.singleton.display();

	}
	

	/**
	 * tests backpropagation with one unique piece of evidence in the graph.
	 */
	@Test
	public void testBackwardsAndForwardsInferenceFromEvidenceOneComplete() {
		
		BayesianNetwork<NodeCategorical> bn = loadGerlandNetwork();
		
		SimpleConditionningInferenceEngine ie = new SimpleConditionningInferenceEngine(bn);
		
		ie.addEvidence("type_nonsalarie", "independant");

		
		// direct backwards
		assertEqualsBD(
				1., 
				ie.getConditionalProbability("salarie", "nonsalarie"),
				4
				);

		assertEqualsBD(
				0.1965, 
				ie.getConditionalProbability("gender", "female"),
				4
				);
		
		// backwards then forward
		assertEqualsBD(
				1., 
				ie.getConditionalProbability("type_salarie", "nonpertinent"),
				4
				);
		assertEqualsBD(
				0., 
				ie.getConditionalProbability("type_salarie", "cdd"),
				4
				);
		
		assertEqualsBD(
				1., 
				ie.getConditionalProbability("actif", "oui"),
				4
				);
		
		assertEqualsBD(
				0., 
				ie.getConditionalProbability("age6", "from0to14"),
				4
				);
		assertEqualsBD(
				.224, 
				ie.getConditionalProbability("age6", "from14to29"),
				2
				);
		assertEqualsBD(
				.3012, 
				ie.getConditionalProbability("age6", "from30to44"),
				4
				);
		assertEqualsBD(
				.3625, 
				ie.getConditionalProbability("age6", "from45to59"),
				4
				);
		assertEqualsBD(
				.1104, 
				ie.getConditionalProbability("age6", "from60to74"),
				4
				);
		assertEqualsBD(
				.0, 
				ie.getConditionalProbability("age6", "from75to89"),
				4
				);
		assertEqualsBD(
				.0019, 
				ie.getConditionalProbability("age6", "from90toMore"),
				4
				);
		
		assertEqualsBD(
				.0, 
				ie.getConditionalProbability("age3", "unknown"),
				4
				);
		assertEqualsBD(
				.1484, 
				ie.getConditionalProbability("age3", "from15to24"),
				4
				);
		assertEqualsBD(
				.6517, 
				ie.getConditionalProbability("age3", "from25to54"),
				4
				);
		assertEqualsBD(
				.1999, 
				ie.getConditionalProbability("age3", "from55to64"),
				4
				);
		
		InferencePerformanceUtils.singleton.display();

		
	}
	

}
