package gospl.algo.bn;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class TestBarley extends AbstractTestAnyBayesianNetwork {

	public TestBarley() {
		super("./src/test/resources/bayesiannetworks/barley.xmlbif");

	}


	@Test
	public void testComputeAll() {
		
		// do nothing
		// cannot compute such a large network with the basic inference engine
	}
	
	/*
	@Test
	public void testInferenceTwo() {
		
		super.testInference(
				new HashMap<String,String>(){{
					put("PIP3", "1");
					put("Akt", "2");
				}}, 
				new HashMap<String,Map<String,Double>>(){{
					put("Plcg", new HashMap<String,Double>() {{ 
						put("2", 0.0284);
						put("3", 0.1940);
					}});
					put("Jnk", new HashMap<String,Double>() {{
						put("1", 0.5309);
					}});
				}}
				);
		
	}
	*/
}
