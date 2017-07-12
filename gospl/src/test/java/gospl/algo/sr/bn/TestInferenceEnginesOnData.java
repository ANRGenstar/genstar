package gospl.algo.sr.bn;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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

import core.util.random.GenstarRandomUtils;

/**
 * Tips: run with JVM parameters -XX:+PrintGCDetails to debug memory garbage collecting  
 * 
 * @author Samuel Thiriot
 *
 */
@RunWith(Parameterized.class)
public class TestInferenceEnginesOnData {

	
	@Parameters(name="{0} on {1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {

			{ SimpleConditionningInferenceEngine.class, 	new DataSprinkler() },
			{ EliminationInferenceEngine.class, 			new DataSprinkler() },
			{ RecursiveConditionningEngine.class, 			new DataSprinkler() },
			{ BestInferenceEngine.class, 					new DataSprinkler() },

			{ SimpleConditionningInferenceEngine.class, 	new DataGerland1() },
			{ EliminationInferenceEngine.class, 			new DataGerland1() },
			{ RecursiveConditionningEngine.class, 			new DataGerland1() },
			{ BestInferenceEngine.class, 					new DataGerland1() },

			{ SimpleConditionningInferenceEngine.class, 	new DataCancerSmall() },
			{ EliminationInferenceEngine.class, 			new DataCancerSmall() },
			{ RecursiveConditionningEngine.class, 			new DataCancerSmall() },
			{ BestInferenceEngine.class, 					new DataCancerSmall() },

			{ SimpleConditionningInferenceEngine.class, 	new DataSachs() },
			{ EliminationInferenceEngine.class, 			new DataSachs() },
			{ RecursiveConditionningEngine.class, 			new DataSachs() },
			{ BestInferenceEngine.class, 					new DataSachs() },

			// the complexity of this large case excludes much engines on large data
			// we can activate those but not on build machines in which they would slow don't the process
			
			// // { SimpleConditionningInferenceEngine.class, 	new DataAlarm() },
			{ EliminationInferenceEngine.class, 			new DataAlarm() },
			//{ RecursiveConditionningEngine.class, 			new DataAlarm() },
			// { BestInferenceEngine.class, 					new DataAlarm() },

			// the complexity of this large case excludes these engines
			// //{ SimpleConditionningInferenceEngine.class, 	new DataBarley() },
			// { EliminationInferenceEngine.class, 				new DataBarley() },
			// //{ RecursiveConditionningEngine.class, 			new DataBarley() },
			// { BestInferenceEngine.class, 					new DataBarley() },

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


	@Test(timeout=40000)
	public void testPriors() {
		
		InferencePerformanceUtils.singleton.reset();

		for (Map.Entry<String,Map<String,Double>> variable2expected: data.variables2expectedPriors.entrySet()) {
		
			// assert evidence
			String var = variable2expected.getKey();
			
			try {
				// try questionning the inference engine using factors
				Set<String> vars = new HashSet<>(2);
				vars.add(var);
				Factor factor = ie.computeFactorPriorMarginalsFromString(vars);
				
				// then check results
				Map<String,Double> expected = variable2expected.getValue();
				for (String value: expected.keySet()) {
					Double p = expected.get(value);
					assertEquals(
							"for dataset "+data.name+", expecting p("+var+"="+value+")="+p,
							p, 
							factor.hasUniqueValue()?factor.getUniqueValue():factor.get(var,value),
							Math.pow(1, -data.precision)
							);
				}
				
			} catch (UnsupportedOperationException e) {
				
				// or check the result 
				
				Map<String,Double> expected = variable2expected.getValue();
				for (String value: expected.keySet()) {
					Double p = expected.get(value);
					assertEquals(
							"for dataset "+data.name+", expecting p("+var+"="+value+")="+p,
							p, 
							ie.getConditionalProbability(var, value),
							Math.pow(1, -data.precision)
							);
				}
				//throw new UnsupportedOperationException("inference engine "+ieClass.getName()+" does not support the computation of prior marginals as factors");
			}
			
			
			ie.clearEvidence();
			
		}
	}
		

	@Test //(timeout=60000)
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
		List<NodeCategorical> toQuery = varsOrdered.subList(0, Math.min(5, evidenceToSet));
		System.err.println("will query for test the variables "+toQuery.stream().map(v->v.name).collect(Collectors.joining(",")));
		
		System.err.flush();
		
		// now iterate these variables and question their values
		for (NodeCategorical n: toQuery) {
			double total = 0.;
			for (String s: n.getDomain()) {
				double d = ie.getConditionalProbability(n, s);
				System.err.println("p("+n.name+"="+s+"|evidence)="+d);
				total = total += d;
			}
			System.err.println("=> p("+n.name+"=*|evidence)="+total);
		}
	
		System.err.flush();

		InferencePerformanceUtils.singleton.display();
		ie.clearEvidence();
		InferencePerformanceUtils.singleton.reset();
		
	}
	
	@Test //(timeout=60000)
	public void testEvidencePropagation() {
		
		InferencePerformanceUtils.singleton.reset();

		for (Map.Entry<Map<String,String>,Map<String,Map<String,Double>>> evidence2expected: data.evidence2expectedResults.entrySet()) {
		
			// assert evidence
			Map<String,String> evidence = evidence2expected.getKey();
			for (Map.Entry<String,String> k2v: evidence.entrySet()) {
				System.err.println("set evidence: "+k2v.getKey()+" = "+k2v.getValue());
				ie.addEvidence(k2v.getKey(), k2v.getValue());
			}

			// ensure evidence is respected 
			for (Map.Entry<String,String> k2v: evidence.entrySet()) {
				ie.addEvidence(k2v.getKey(), k2v.getValue());
				assertEquals(
						"for inference engine "+ieClass.getSimpleName()+" and dataset "+data.name+", expecting p("+k2v.getKey()+"="+k2v.getValue()+"|direct evidence)=1.0",
						1., 
						ie.getConditionalProbability(k2v.getKey(), k2v.getValue()),
						Math.pow(1, -data.precision)
						);
				for (String other: bn.getVariable(k2v.getKey()).getDomain()) {
					if (other.equals(k2v.getValue()))
						continue;
					assertEquals(
							"for inference engine "+ieClass.getSimpleName()+" and dataset "+data.name+", expecting p("+k2v.getKey()+"="+other+"|direct evidence)=0.0",
							1., 
							ie.getConditionalProbability(k2v.getKey(), other),
							Math.pow(1, -data.precision)
							);
				}
			}
			
			// then check results
			Map<String,Map<String,Double>> expected = evidence2expected.getValue();
			for (String s: expected.keySet()) {
				for (String value: expected.get(s).keySet()) {
					Double p = expected.get(s).get(value);
					assertEquals(
							"for inference engine "+ieClass.getSimpleName()+" and dataset "+data.name+", expecting p("+s+"="+value+"|"+evidence+")="+p,
							p, 
							ie.getConditionalProbability(s, value),
							Math.pow(1, -data.precision)
							);
				}
			}
			
			InferencePerformanceUtils.singleton.display();

			ie.clearEvidence();
			InferencePerformanceUtils.singleton.reset();

		}
		
	}
	
	@Test(timeout=20000)
	public void testGenerateWithEvidence() {

		while (true) {
			

				ie.clearEvidence();
				InferencePerformanceUtils.singleton.reset();
				
				// define some evidence to be asserted for any individual
				// sets evidence for the two last nodes of the network
				List<NodeCategorical> varsOrdered = bn.enumerateNodes();
				int evidenceToSet = bn.getNodes().size()/2;
				Map<NodeCategorical,String> systematicEvidence = new HashMap<>();
				
				for (int i=0; i<evidenceToSet; i++) {
					
					// take the one variable from the last of the network (to test retropropagation, the most difficult case)
					NodeCategorical n = varsOrdered.get(varsOrdered.size()-i-1);
					
	
					// take one value which does not has probability 0
					String v = GenstarRandomUtils.oneOf(n.getDomain());
					
					systematicEvidence.put(n, v);
					ie.addEvidence(systematicEvidence);
						
					System.err.println("will define evidence "+n.name+"="+v);
		
				}
				
				System.out.println("free memory: "+Runtime.getRuntime().freeMemory()/1024/1024+"Mb");
				System.out.flush();

				try {

				ie.addEvidence(systematicEvidence);
		
				for (int i=0; i<100; i++) {
					
	
					// generate one
					Map<NodeCategorical,String> node2attribute = ie.sampleOne();
					
					// we finished an individual
					System.err.println(i+": "+node2attribute.entrySet().stream().map(e -> e.getKey().name+"="+e.getValue()).collect(Collectors.joining(",\t")));
					
						// ensure systematic evidence is enforced
						for (Map.Entry<NodeCategorical,String> e: systematicEvidence.entrySet()) {
							assertEquals(
									"the generation feature of engine "+ieClass.getSimpleName()+" on data "+data.name+" should enforce prior evidence during generation, but it was not on "+e.getKey().name+"="+e.getValue(),
									e.getValue(),
									node2attribute.get(e.getKey())
									);
						}
					
							
				}
				
				InferencePerformanceUtils.singleton.display();
				
				break;
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				System.err.println("got an error with evidence "+systematicEvidence+", probably because Pr(evidence)=0; retrying with another random one");
			}
		}
	}


	
	@Test(timeout=40000)
	public void testGenerate() {
		
		ie.clearEvidence();
		InferencePerformanceUtils.singleton.reset();
		
		for (int i=0; i<100; i++) {
			Map<NodeCategorical,String> node2attribute = ie.sampleOne();
			
			// we finished an individual
			System.err.println(i+": "+node2attribute.entrySet().stream().map(e -> e.getKey().name+"="+e.getValue()).collect(Collectors.joining(",\t")));
			System.err.flush();
			System.out.println("free memory: "+Runtime.getRuntime().freeMemory()/1024/1024+"Mb");
			System.out.flush();
		}
		
		InferencePerformanceUtils.singleton.display();
		
	}
	

}
