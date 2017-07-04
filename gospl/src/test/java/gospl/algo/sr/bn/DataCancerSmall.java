package gospl.algo.sr.bn;

import java.util.HashMap;
import java.util.Map;

public class DataCancerSmall extends AbstractTestData {

	public DataCancerSmall() {
		super(
				"cancer small", 
				"./src/test/resources/bayesiannetworks/cancer.xmlbif", 
				3
				);
		
		// add expected data
		Map<String,String> evidence = null;
		
		// with no inference
		// TODO ?
		
		// with evidence
		
		// case 1
		evidence = new HashMap<>();
		evidence.put("Pollution", "high");
		evidence.put("Smoker", "True");
		this.addExpectedPosteriorForEvidence(evidence, "Cancer", "False", 0.95);
		this.addExpectedPosteriorForEvidence(evidence, "Xray", "positive", 0.2350);
		
		// case 2
		evidence = new HashMap<>();
		evidence.put("Pollution", "high");
		evidence.put("Smoker", "True");
		evidence.put("Xray", "positive");
		this.addExpectedPosteriorForEvidence(evidence, "Cancer", "True", 0.1915);
		this.addExpectedPosteriorForEvidence(evidence, "Xray", "positive", 1.);


	}

}
