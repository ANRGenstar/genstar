package gospl.algo.sr.bn;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DataBarley extends AbstractTestData {

	public DataBarley() {
		super(
				"Barley", 
				"./src/test/resources/bayesiannetworks/barley.xmlbif", 
				3
				);
		
		// add expected data
		Map<String,String> evidence = null;
		
		// with no inference
		evidence = Collections.emptyMap();
		this.addExpectedPosteriorForEvidence(evidence, "antplnt", "x_175", 0.185045);
		this.addExpectedPosteriorForEvidence(evidence, "forfrugt", "Cereals", 0.2);
		this.addExpectedPosteriorForEvidence(evidence, "nprot", "x_160", 0.013965);
		
		// with evidence
		
		// case 1
		evidence = new HashMap<>();
		evidence.put("nprot", "x_160");
		evidence.put("ntilg", "x_75");
		this.addExpectedPosteriorForEvidence(evidence, "forfrugt", "Cereals", 0.3710);
		this.addExpectedPosteriorForEvidence(evidence, "forfrugt", "Beets", 0.1441);
		this.addExpectedPosteriorForEvidence(evidence, "protein", "x_9", 0.0093);
		this.addExpectedPosteriorForEvidence(evidence, "jordn", "x_120", 0.0022);



	}

}
