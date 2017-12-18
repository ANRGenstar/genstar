package gospl.algo.sr.bn;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import core.configuration.dictionary.DemographicDictionary;
import core.configuration.dictionary.IGenstarDictionary;
import core.metamodel.attribute.demographic.DemographicAttribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.io.GSSurveyType;
import core.metamodel.io.IGSSurvey;
import core.metamodel.value.IValue;
import core.util.excpetion.GSIllegalRangedData;
import gospl.GosplPopulation;
import gospl.distribution.GosplInputDataManager;
import gospl.io.CsvInputHandler;
import gospl.io.GosplSurveyFactory;
import gospl.io.exception.InvalidSurveyFormatException;
import gospl.io.util.ReadDictionaryUtils;

public class TestBayesianNetworkCompletionSampler {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	private void testCompletion(Class<? extends AbstractInferenceEngine> engineClass) {
		
		File fileCSVsample = new File("./src/test/resources/gerland_sample_incomplete.csv");
		File fileBN = new File("./src/test/resources/bayesiannetworks/gerland.xbif");
		
		// we have to load a BN
		CategoricalBayesianNetwork bn = CategoricalBayesianNetwork.loadFromXMLBIF(fileBN);
		
		// to read the incomplete sample, we need a dict
		IGenstarDictionary<DemographicAttribute<? extends IValue>> dictionary = 
				ReadDictionaryUtils.readBayesianNetworkAsDictionary(bn);
		
		// we need an incomplete sample
		char sep;
		try {
			sep = CsvInputHandler.detectSeparator(fileCSVsample);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		// configure the survey factory with the right parameters
		GosplSurveyFactory factory = new GosplSurveyFactory();
		IGSSurvey survey;
		try {
			survey = factory.getSurvey(
					fileCSVsample.getAbsolutePath(), 
					0,
					sep,
					1,
					0,
					GSSurveyType.Sample,
					GosplSurveyFactory.CSV_EXT
					);
		} catch (InvalidFormatException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (InvalidSurveyFormatException e) {
			throw new IllegalArgumentException("Invalid survey format", e);
		}
		
		GosplPopulation pop = null;
		
		IGenstarDictionary<DemographicAttribute<? extends IValue>> updaptedDictionary = 
					new DemographicDictionary<>(dictionary);

		try {
			//Map<String,String> keepOnlyEqual = new HashMap<>();
			//keepOnlyEqual.put("DEPT", "75");
			//keepOnlyEqual.put("NAT13", "Marocains");
			
			
			pop = GosplInputDataManager.getSample(
					survey, 
					updaptedDictionary, 
					null,
					Collections.emptyMap() // TODO parameters for that
					);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (InvalidSurveyFormatException e) {
			throw new RuntimeException(e);
		}
		
		
		// then we can ask for a more complete population
		BayesianNetworkCompletionSampler sampler ;
		try {
			AbstractInferenceEngine engine = engineClass.getDeclaredConstructor(CategoricalBayesianNetwork.class).newInstance(bn);
			sampler = new BayesianNetworkCompletionSampler(bn, engine);
		} catch (GSIllegalRangedData e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		}
		
		for (ADemoEntity e: pop) {
			
			System.err.println("before:\t"+e);
			ADemoEntity novelEntity = sampler.complete(e);
			
			// we don't want the entities to be replaced in place 
			// TODO assertNotEquals(e, novelEntity);
			
			
			System.err.println("after:\t"+novelEntity);
			
			System.err.println();
			
			// check it
			
			// should be the expected size
			assertEquals(bn.getNodes().size(), novelEntity.getAttributes().size());
			
			// the probability of that result should not be null
			
			
		}
		
		//fail("Not yet implemented");
	}
	
	@Test
	public void testCompletionRecursive() {
		this.testCompletion(RecursiveConditionningEngine.class);
	}
	
	@Test
	public void testCompletionEliminationInference() {
		this.testCompletion(EliminationInferenceEngine.class);
	}

}
