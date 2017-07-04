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
	protected CategoricalBayesianNetwork loadFile() {

		return CategoricalBayesianNetwork.loadFromXMLBIF(f);
		
	}
	

	@Test
	public void testLoadNetwork() {
		CategoricalBayesianNetwork bn = loadFile();
	}
	
	public void testInferenceSimpleConditionning(Map<String,String> evidence, Map<String,Map<String,Double>> expected) {
		
		CategoricalBayesianNetwork bn = loadFile();
		
		SimpleConditionningInferenceEngine ie = new SimpleConditionningInferenceEngine(bn);

		testInference(bn, ie, evidence, expected);
	}
	

	public void testInferenceElimination(Map<String,String> evidence, Map<String,Map<String,Double>> expected) {
		
		CategoricalBayesianNetwork bn = loadFile();
		
		EliminationInferenceEngine ie = new EliminationInferenceEngine(bn);

		testInference(bn, ie, evidence, expected);
	}

	public void testInference(CategoricalBayesianNetwork bn, AbstractInferenceEngine ie, Map<String,String> evidence, Map<String,Map<String,Double>> expected) {
		
		
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
		
		CategoricalBayesianNetwork bn = loadFile();

		SimpleConditionningInferenceEngine ie = new SimpleConditionningInferenceEngine(bn);
		
		ie.computeAll();
		
		InferencePerformanceUtils.singleton.display();

		
	}
	
	@Test
	public void testGenerateWithSimpleConditionning() {

		CategoricalBayesianNetwork bn = loadFile();

		testGenerateWith(bn, new SimpleConditionningInferenceEngine(bn));
	}
	
	@Test
	public void testGenerateWithElimination() {

		CategoricalBayesianNetwork bn = loadFile();

		testGenerateWith(bn, new EliminationInferenceEngine(bn));
	}
	
	public void testGenerateWith(CategoricalBayesianNetwork bn, AbstractInferenceEngine ie) {
	

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
