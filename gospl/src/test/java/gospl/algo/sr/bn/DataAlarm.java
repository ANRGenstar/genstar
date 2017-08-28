package gospl.algo.sr.bn;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DataAlarm extends AbstractTestData {

	public DataAlarm() {
		super(
				"alarm", 
				"./src/test/resources/bayesiannetworks/alarm.xmlbif", 
				5
				);
		

		// add expected data
		Map<String,String> evidence = null;
		
		// with no inference
		evidence = Collections.emptyMap();
		addExpectedPrior("PAP", "LOW", .0496);
		addExpectedPrior("STROKEVOLUME", "HIGH", .778799);
		addExpectedPrior("SHUNT", "HIGH", .10309499);
		addExpectedPrior("CO", "NORMAL", .18446735);
		
		// with evidence
		
		// case 1
		evidence = new HashMap<>();
		evidence.put("CO", "LOW");
		this.addExpectedPosteriorForEvidence(evidence, "PVSAT", "NORMAL", 0.0039351);
		this.addExpectedPosteriorForEvidence(evidence, "FIO2", "LOW", 0.00860472);
		this.addExpectedPosteriorForEvidence(evidence, "SHUNT", "NORMAL", 0.154551);
		this.addExpectedPosteriorForEvidence(evidence, "VENTMACH", "ZERO", 0.009004);

		// case 2
		evidence = new HashMap<>();
		evidence.put("ERRCAUTER", "TRUE");
		this.addExpectedPosteriorForEvidence(evidence, "FIO2", "LOW", 0.005);
		this.addExpectedPosteriorForEvidence(evidence, "HREKG", "LOW", 0.006985);
		this.addExpectedPosteriorForEvidence(evidence, "HRSAT", "LOW", 0.006985);

		// test 3
		evidence = new HashMap<>();
		evidence.put("VENTTUBE", "LOW");
		this.addExpectedPosteriorForEvidence(evidence, "CO", "LOW", 0.2218);

	

	}

}
