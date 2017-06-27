package gospl.algo.sr.bn;

import static gospl.algo.sr.bn.JUnitBigDecimals.assertEqualsBD;
import static org.junit.Assert.assertNotNull;

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

public abstract class AbstractTestAnyBayesianNetwork {

	protected final File f;
	
	protected AbstractTestAnyBayesianNetwork(String filename) {
		f = new File(filename);
	
	}
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Loads the network
	 * @return
	 */
	protected BayesianNetwork<NodeCategorical> loadFile() {

		return BayesianNetwork.loadFromXMLBIF(f);
		
	}
	

	@Test
	public void testLoadNetwork() {
		BayesianNetwork<NodeCategorical> bn = loadFile();
	}
	
	public void testInference(Map<String,String> evidence, Map<String,Map<String,Double>> expected) {
		
		BayesianNetwork<NodeCategorical> bn = loadFile();
		
		SimpleConditionningInferenceEngine ie = new SimpleConditionningInferenceEngine(bn);

		// set inference
		for (String nodeName: evidence.keySet()) {
			
			NodeCategorical c = bn.getVariable(nodeName);
			assertNotNull("node not found: "+nodeName, c);
			ie.addEvidence(c, evidence.get(nodeName));
			
		}
		
		// check results
		for (String nodeName: expected.keySet()) {
			
			Map<String,Double> ee = expected.get(nodeName);
			
			for (String v: ee.keySet()) {
				assertEqualsBD(
						ee.get(v), 
						ie.getConditionalProbability(bn.getVariable(nodeName), v), 
						4
						);	
			}
			
		}
		
		InferencePerformanceUtils.singleton.display();

		
	}
	
	@Test
	public void testComputeAll() {
		
		BayesianNetwork<NodeCategorical> bn = loadFile();

		SimpleConditionningInferenceEngine ie = new SimpleConditionningInferenceEngine(bn);
		
		ie.computeAll();
		
		InferencePerformanceUtils.singleton.display();

		
	}
	
	@Test
	public void testGenerate() {
	
		BayesianNetwork<NodeCategorical> bn = loadFile();

		SimpleConditionningInferenceEngine ie = new SimpleConditionningInferenceEngine(bn);
		
		for (int i=0; i<100; i++) {
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
	
}
