package gospl.algo.sr.bn;

import java.util.Collections;
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
		
		// with evidence
		
		// case 1
		


	}

}
