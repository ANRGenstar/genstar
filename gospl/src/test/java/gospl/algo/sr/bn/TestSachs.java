package gospl.algo.sr.bn;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class TestSachs extends AbstractTestAnyBayesianNetwork {

	public TestSachs() {
		super("./src/test/resources/bayesiannetworks/sachs.xmlbif");

	}

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
	
}
