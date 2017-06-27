package gospl.algo.bn;

import static gospl.algo.bn.JUnitBigDecimals.assertEqualsBD;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import core.util.random.GenstarRandom;

public class TestSprinklers {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	protected BayesianNetwork<NodeCategorical> loadGerlandNetwork() {

		File f = new File("./src/test/resources/bayesiannetworks/sprinkler_rain_grasswet.xmlbif");
		
		return BayesianNetwork.loadFromXMLBIF(f);
		
	}
	
	/**
	 * Ensures we can load the network and it contains the data expected for our test
	 */
	@Test
	public void testLoadBasic() {
		
		BayesianNetwork<NodeCategorical> bn = loadGerlandNetwork();
				
		assertEquals(
				0.2, 
				bn.getVariable("rain").getProbability("true").doubleValue(), 
				1e-5
				);
		assertEquals(
				0.01, 
				bn.getVariable("sprinkler").getProbability("true", "rain", "true").doubleValue(), 
				1e-5
				);
		assertEquals(
				0.0, 
				bn.getVariable("grass_wet").getProbability("true", "rain", "false", "sprinkler", "false").doubleValue(), 
				1e-5
				);
		
		Map<NodeCategorical,String> t = new HashMap<>();
		t.put(bn.getVariable("rain"), "false");
		t.put(bn.getVariable("sprinkler"), "false");
		assertEquals(
				1.0, 
				bn.getVariable("grass_wet").getProbability("false", t).doubleValue(), 
				1e-5
				);
		
		
	}

	/**
	 * Ensure we are able to compute the posterior probabilities
	 */
	@Test
	public void testPostProbas() {
		
		BayesianNetwork<NodeCategorical> bn = loadGerlandNetwork();
		
		assertEquals(
				0.2, 
				bn.getVariable("rain").getConditionalProbabilityPosterior("true").doubleValue(), 
				1e-5
				);
		
		assertEquals(
				0.322, 
				bn.getVariable("sprinkler").getConditionalProbabilityPosterior("true").doubleValue(), 
				1e-5
				);
		
		assertEquals(
				0.4041, 
				bn.getVariable("grass_wet").getConditionalProbabilityPosterior("true").doubleValue(), 
				1e-3
				);
		
	}

	/**
	 * Tests the summing of probabilities
	 */
	@Test
	public void testSumProba() {
		
		BayesianNetwork<NodeCategorical> bn = loadGerlandNetwork();
		
		SimpleConditionningInferenceEngine ie = new SimpleConditionningInferenceEngine(bn);
		
		Map<NodeCategorical,String> known = new HashMap<>();
		known.put(bn.getVariable("grass_wet"), "true");
		
		Set<NodeCategorical> nuisance = new HashSet<>(bn.getNodes());
		nuisance.removeAll(known.keySet());
		
		assertEquals(
				0.44838, 
				ie.sumProbabilities(known, nuisance).doubleValue(), 
				1e-5
				);

		known.put(bn.getVariable("rain"), "true");
		nuisance.removeAll(known.keySet());

		assertEquals(
				0.16038, 
				ie.sumProbabilities(known, nuisance).doubleValue(), 
				1e-5
				);
		
	}



	/**
	 * Test backwards inference.
	 * Reference values computed in Samiam with hugin inference
	 */
	@Test
	public void testBackwardsInferenceFromEvidenceOne() {
		
		BayesianNetwork<NodeCategorical> bn = loadGerlandNetwork();
		
		SimpleConditionningInferenceEngine ie = new SimpleConditionningInferenceEngine(bn);

		
		ie.addEvidence("grass_wet", "true");

		assertEqualsBD(
				.6467, 
				ie.getConditionalProbability("sprinkler", "true"),
				4
				);
		
		assertEqualsBD(
				.3533, 
				ie.getConditionalProbability("sprinkler", "false"),
				4
				);
		
		assertEqualsBD(
				.3577, 
				ie.getConditionalProbability("rain", "true"),
				4
				);
		
		assertEqualsBD(
				.6423, 
				ie.getConditionalProbability("rain", "false"),
				4
				);
	}
	


	/**
	 * Test backwards inference.
	 * Reference values computed in Samiam with hugin inference
	 */
	@Test
	public void testBackwardsInferenceFromEvidenceTwo() {
		
		BayesianNetwork<NodeCategorical> bn = loadGerlandNetwork();
		
		SimpleConditionningInferenceEngine ie = new SimpleConditionningInferenceEngine(bn);

		ie.addEvidence("grass_wet", "true");
		ie.addEvidence("sprinkler", "true");

		assertEqualsBD(
				.0068, 
				ie.getConditionalProbability("rain", "true"),
				4
				);
		
		assertEqualsBD(
				.9932, 
				ie.getConditionalProbability("rain", "false"),
				3
				);
	}

}
