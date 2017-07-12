package gospl.algo.sr.bn;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO add expected data to check bn loaddinng
 * 		
		assertEqualsBD(
				0, 
				bn.getVariable("actif").getProbability("unknown", "age3", "from25to54", "gender", "female"), 
				5
				);
		assertEqualsBD(
				0.922, 
				bn.getVariable("actif").getProbability("oui", "age3", "from25to54", "gender", "female"), 
				5
				);
		assertEqualsBD(
				0.078, 
				bn.getVariable("actif").getProbability("non", "age3", "from25to54", "gender", "female"), 
				5
				);
				
 * @author sam
 *
 */
public class DataGerland1 extends AbstractTestData {

	public DataGerland1() {
		super(
				"gerland1", 
				"./src/test/resources/bayesiannetworks/gerland.xbif", 
				4
				);
		
		
		// add expected data
		Map<String,String> evidence = null;
		
		// with no inference
		evidence = Collections.emptyMap();
		addExpectedPrior("type_salarie", "nonpertinent", .60643944);
		
		// with evidence
		
		// case 1
		evidence = new HashMap<>();
		evidence.put("age3", "from15to24");
		this.addExpectedPosteriorForEvidence(evidence, "age6", "from14to29", 0.3902);
		
		// case 2
		evidence = new HashMap<>();
		evidence.put("age3", "from15to24");
		evidence.put("type_salarie", "apprentissage_stage");
		this.addExpectedPosteriorForEvidence(evidence, "gender", "male", 0.971);
		this.addExpectedPosteriorForEvidence(evidence, "salarie", "salarie", 1.);

		// test 3
		evidence = new HashMap<>();
		evidence.put("age3", "from15to24");
		evidence.put("type_salarie", "apprentissage_stage");
		evidence.put("age6", "from90toMore");
		this.addExpectedPosteriorForEvidence(evidence, "gender", "male", 0.9945);

		// test 4
		evidence = new HashMap<>();
		evidence.put("actif", "oui");
		evidence.put("type_salarie", "cdd");
		// ... these nodes are directly constrained by evidence
		this.addExpectedPosteriorForEvidence(evidence, "gender", "female", 0.5880);
		this.addExpectedPosteriorForEvidence(evidence, "salarie", "salarie", 1.);
		this.addExpectedPosteriorForEvidence(evidence, "age3", "from25to54", .6362);
		// ... these nodes are constrained by known probabilities of their children known
		this.addExpectedPosteriorForEvidence(evidence, "age6", "from14to29", .1995);
		this.addExpectedPosteriorForEvidence(evidence, "type_nonsalarie", "nonpertinent", 1.);

		// test 5
		evidence = new HashMap<>();
		evidence.put("type_nonsalarie", "independant");
		// ... direct backwards
		this.addExpectedPosteriorForEvidence(evidence, "salarie", "nonsalarie", 1.);
		this.addExpectedPosteriorForEvidence(evidence, "gender", "female", 0.1965);
		// backwards then forward
		this.addExpectedPosteriorForEvidence(evidence, "type_salarie", "nonpertinent", 1.);
		this.addExpectedPosteriorForEvidence(evidence, "type_salarie", "cdd", 0.);
		this.addExpectedPosteriorForEvidence(evidence, "actif", "oui", 1.);
		this.addExpectedPosteriorForEvidence(evidence, "age6", "from0to14", 0.);
		this.addExpectedPosteriorForEvidence(evidence, "age6", "from14to29", .224);
		this.addExpectedPosteriorForEvidence(evidence, "age6", "from30to44", .3012);
		this.addExpectedPosteriorForEvidence(evidence, "age6", "from45to59", .3625);
		this.addExpectedPosteriorForEvidence(evidence, "age6", "from60to74", .1104);
		this.addExpectedPosteriorForEvidence(evidence, "age6", "from75to89", .0);
		this.addExpectedPosteriorForEvidence(evidence, "age6", "from90toMore", .0019);
		this.addExpectedPosteriorForEvidence(evidence, "age3", "unknown", .0);
		this.addExpectedPosteriorForEvidence(evidence, "age3", "from15to24", .1484);
		this.addExpectedPosteriorForEvidence(evidence, "age3", "from25to54", .6517);
		this.addExpectedPosteriorForEvidence(evidence, "age3", "from55to64", .1999);

	}

}
