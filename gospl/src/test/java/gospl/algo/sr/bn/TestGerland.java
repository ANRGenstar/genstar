package gospl.algo.sr.bn;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import core.util.random.GenstarRandom;

public class TestGerland {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	protected CategoricalBayesianNetwork loadGerlandNetwork() {

		File f = new File("./src/test/resources/bayesiannetworks/gerland.xbif");
		
		return CategoricalBayesianNetwork.loadFromXMLBIF(f);
		
	}
	
	
	@Test
	public void testGenerate() {

		CategoricalBayesianNetwork bn = loadGerlandNetwork();
		
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
	


	
}
