package gospl.algo.sr.bn;

import static gospl.algo.sr.bn.JUnitBigDecimals.assertEqualsBD;

import java.io.File;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import core.util.random.GenstarRandom;


@RunWith(Parameterized.class)
public class TestInferenceEnginesOnData {

	@Parameters(name="{0} on {1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {

			{ SimpleConditionningInferenceEngine.class, 	new DataSprinkler() },
			{ EliminationInferenceEngine.class, 			new DataSprinkler() },
			{ RecursiveConditionningEngine.class, 			new DataSprinkler() },
			
			{ SimpleConditionningInferenceEngine.class, 	new DataGerland1() },
			{ EliminationInferenceEngine.class, 			new DataGerland1() },
			{ RecursiveConditionningEngine.class, 			new DataGerland1() },
 
			{ SimpleConditionningInferenceEngine.class, 	new DataCancerSmall() },
			{ EliminationInferenceEngine.class, 			new DataCancerSmall() },
			{ RecursiveConditionningEngine.class, 			new DataCancerSmall() },

			{ SimpleConditionningInferenceEngine.class, 	new DataSachs() },
			{ EliminationInferenceEngine.class, 			new DataSachs() },
			{ RecursiveConditionningEngine.class, 			new DataSachs() },

			{ SimpleConditionningInferenceEngine.class, 	new DataBarley() },
			{ EliminationInferenceEngine.class, 			new DataBarley() },
			{ RecursiveConditionningEngine.class, 			new DataBarley() },

           });
    }
    
    @Parameter(0)
    public Class ieClass;
    
    @Parameter(1)
    public AbstractTestData data;
    
    private AbstractInferenceEngine ie;
    
    private CategoricalBayesianNetwork bn;
    
	@Before
	public void setUp() throws Exception {
		
		System.out.println("\n\n========================================================================================================");

		System.out.println("testing dataset "+data.name);
		System.out.println("loading file "+data.filename);
		bn = CategoricalBayesianNetwork.loadFromXMLBIF(new File(data.filename));
		System.out.println("creating an instance of inference engine "+ieClass.getCanonicalName());
		System.out.println("========================================================================================================");

		System.out.flush();
		ie = (AbstractInferenceEngine) ieClass.getConstructor(CategoricalBayesianNetwork.class).newInstance(bn);
		
	}

	@After
	public void tearDown() throws Exception {
	}


	@Test(timeout=1000)
	public void testPriors() {
		
		InferencePerformanceUtils.singleton.reset();

		for (Map.Entry<Set<String>,Map<String,Map<String,Double>>> evidence2expected: data.variables2expectedPriors.entrySet()) {
		
			// assert evidence
			Set<String> vars = evidence2expected.getKey();
			
			Factor factor = null;
			try {
				factor = ie.computeFactorPriorMarginalsFromString(vars);
			} catch (UnsupportedOperationException e) {
				break;
				//throw new UnsupportedOperationException("inference engine "+ieClass.getName()+" does not support the computation of prior marginals as factors");
			}
			
			// then check results
			Map<String,Map<String,Double>> expected = evidence2expected.getValue();
			for (String s: expected.keySet()) {
				for (String value: expected.get(s).keySet()) {
					Double p = expected.get(s).get(value);
					assertEqualsBD(
							"for dataset "+data.name+", expecting p("+s+"="+value+")="+p,
							p, 
							factor.get(s,value),
							data.precision
							);
				}
			}
			
			ie.clearEvidence();
			
		}
	}
		

	@Test //(timeout=40000)
	public void testEvidencePropagationAndManyRandomQueries() {
		
		InferencePerformanceUtils.singleton.reset();
		ie.clearEvidence();

		// sets evidence for the two last nodes of the network
		List<NodeCategorical> varsOrdered = bn.enumerateNodes();
		int evidenceToSet = bn.getNodes().size()/2;
		for (int i=0; i<evidenceToSet; i++) {
			// take the one variable from the last of the network (to test retropropagation, the most difficult case)
			NodeCategorical n = varsOrdered.get(varsOrdered.size()-i-1);
			// take one value which does not has probability 0
			String v = null;
			v = n.getDomain().get(n.getDomainSize()-1);
			/*for (String cv: n.getDomain()) {
				if (BigDecimal.ZERO.compareTo(n.getConditionalProbabilityPosterior(cv)) < 0) {
					v = cv;
					break;
				}
			}
			*/
			//String v = GenstarRandomUtils.oneOf(n.getDomain().stream().filter(m ->)<0).collect(Collectors.toList()));
			System.err.println("will define evidence "+n.name+"="+v);
			ie.addEvidence(n, v);
		}
		
		// define which variables we will query
		List<NodeCategorical> toQuery = varsOrdered.subList(0, evidenceToSet);
		System.err.println("will query for test the variables "+toQuery);
		
		System.err.flush();
		
		// now iterate these variables and question their values
		for (int i=0; i<3; i++) {
			for (NodeCategorical n: toQuery) {
				BigDecimal total = BigDecimal.ZERO;
				for (String s: n.getDomain()) {
					BigDecimal d = ie.getConditionalProbability(n, s);
					System.err.println("p("+n.name+"="+s+"|evidence)="+d);
					total = total.add(d);
				}
				System.err.println("=> p("+n.name+"=*|evidence)="+total);
			}
		}
		
		System.err.flush();

		InferencePerformanceUtils.singleton.display();
		ie.clearEvidence();
		InferencePerformanceUtils.singleton.reset();
		
	}
	
	@Test(timeout=20000)
	public void testEvidencePropagation() {
		
		InferencePerformanceUtils.singleton.reset();

		for (Map.Entry<Map<String,String>,Map<String,Map<String,Double>>> evidence2expected: data.evidence2expectedResults.entrySet()) {
		
			// assert evidence
			Map<String,String> evidence = evidence2expected.getKey();
			for (Map.Entry<String,String> k2v: evidence.entrySet()) {
				System.out.println("set evidence: "+k2v.getKey()+" = "+k2v.getValue());
				ie.addEvidence(k2v.getKey(), k2v.getValue());
			}

			// ensure evidence is respected 
			for (Map.Entry<String,String> k2v: evidence.entrySet()) {
				ie.addEvidence(k2v.getKey(), k2v.getValue());
				assertEqualsBD(
						"for inference engine "+ieClass.getSimpleName()+" and dataset "+data.name+", expecting p("+k2v.getKey()+"="+k2v.getValue()+"|direct evidence)=1.0",
						BigDecimal.ONE, 
						ie.getConditionalProbability(k2v.getKey(), k2v.getValue()),
						data.precision
						);
				for (String other: bn.getVariable(k2v.getKey()).getDomain()) {
					if (other.equals(k2v.getValue()))
						continue;
					assertEqualsBD(
							"for inference engine "+ieClass.getSimpleName()+" and dataset "+data.name+", expecting p("+k2v.getKey()+"="+other+"|direct evidence)=0.0",
							BigDecimal.ZERO, 
							ie.getConditionalProbability(k2v.getKey(), other),
							data.precision
							);
				}
			}
			
			// TODO add test inference proba evidence == 1

			// then check results
			Map<String,Map<String,Double>> expected = evidence2expected.getValue();
			for (String s: expected.keySet()) {
				for (String value: expected.get(s).keySet()) {
					Double p = expected.get(s).get(value);
					assertEqualsBD(
							"for inference engine "+ieClass.getSimpleName()+" and dataset "+data.name+", expecting p("+s+"="+value+"|"+evidence+")="+p,
							p, 
							ie.getConditionalProbability(s, value),
							data.precision
							);
				}
			}
			
			InferencePerformanceUtils.singleton.display();

			ie.clearEvidence();
			InferencePerformanceUtils.singleton.reset();

		}
		
	}
	

	@Test(timeout=20000)
	public void testGenerate() {
		
		InferencePerformanceUtils.singleton.reset();
		
		for (int i=0; i<1000; i++) {
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
			System.err.println(i+": "+node2attribute.entrySet().stream().map(e -> e.getKey().name+"="+e.getValue()).collect(Collectors.joining(",\t")));
		}
		
		InferencePerformanceUtils.singleton.display();
		
	}
	

}
