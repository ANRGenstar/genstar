package gospl.algo.bn;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class TestCancerSmall extends AbstractTestAnyBayesianNetwork {

	public TestCancerSmall() {
		super("./src/test/resources/bayesiannetworks/cancer.xmlbif");

	}

	@Test
	public void testInferenceTwo() {
		
		super.testInference(
				new HashMap<String,String>(){{
					put("Pollution", "high");
					put("Smoker", "True");
				}}, 
				new HashMap<String,Map<String,Double>>(){{
					put("Cancer", new HashMap<String,Double>() {{ 
						put("False", 0.95);
					}});
					put("Xray", new HashMap<String,Double>() {{
						put("positive", 0.2350);
					}});
				}}
				);
		
	}
	

	@Test
	public void testInferenceThree() {
		
		super.testInference(
				new HashMap<String,String>(){{
					put("Pollution", "high");
					put("Smoker", "True");
					put("Xray", "positive");
				}}, 
				new HashMap<String,Map<String,Double>>(){{
					put("Cancer", new HashMap<String,Double>() {{ 
						put("True", 0.1915);
					}});
					put("Xray", new HashMap<String,Double>() {{
						put("positive", 1.);
					}});
				}}
				);
		
	}
}
