package gospl.algo.sr.bn;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DataSprinkler extends AbstractTestData {

	public DataSprinkler() {
		super(
				"sprinkler", 
				"./src/test/resources/bayesiannetworks/sprinkler_rain_grasswet.xmlbif", 
				3
				);
		
		// add expected data
		Map<String,String> evidence = null;
		
		// with no inference
		evidence = Collections.emptyMap();
		this.addExpectedPosteriorForEvidence(evidence, "rain", "true", 0.2);
		this.addExpectedPosteriorForEvidence(evidence, "sprinkler", "true", 0.322);

		// with evidence
		
		// case 1
		evidence = new HashMap<>();
		evidence.put("grass_wet", "true");
		this.addExpectedPosteriorForEvidence(evidence, "sprinkler", "true", 0.6467);
		this.addExpectedPosteriorForEvidence(evidence, "sprinkler", "false", 0.3533);
		this.addExpectedPosteriorForEvidence(evidence, "rain", "true", 0.3577);
		this.addExpectedPosteriorForEvidence(evidence, "rain", "false", 0.6423);

		// case 2
		evidence = new HashMap<>();
		evidence.put("grass_wet", "true");
		evidence.put("sprinkler", "true");
		this.addExpectedPosteriorForEvidence(evidence, "rain", "true", 0.0068);
		this.addExpectedPosteriorForEvidence(evidence, "rain", "false", 0.9932);


	}

}
