package gospl.algo.sampler;

import java.io.IOException;

import org.junit.Test;

import core.io.exception.InvalidFileTypeException;
import gospl.algo.HierarchicalHypothesisAlgo;
import gospl.algo.IDistributionInferenceAlgo;
import gospl.distribution.GosplDistributionFactory;
import gospl.metamodel.configuration.GosplConfigurationFile;

public class TestHierarchicalSampler extends AbstractTestBasedOnRouenCase<IHierarchicalSampler> {


	@Override
	protected IDistributionInferenceAlgo<IHierarchicalSampler> getInferenceAlgoToTest() {
		return new HierarchicalHypothesisAlgo();
	}
	
	@Override
	protected IHierarchicalSampler getSamplerToTest() {
		return new GosplHierarchicalSampler();
	}

	@Test
	public void testDirect() {
		
		// parameters of the test
		int targetPopulationSize = 100;
		GosplConfigurationFile confFile = this.getConfigurationFile();

		

		// INSTANCIATE FACTORY
		GosplDistributionFactory df = new GosplDistributionFactory(confFile);
		
		// RETRIEV INFORMATION FROM DATA IN FORM OF A SET OF JOINT DISTRIBUTIONS
		try {
			df.buildDistributions();
		} catch (final RuntimeException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		} catch (final InvalidFileTypeException e) {
			e.printStackTrace();
		}
		

		// TRANSPOSE SAMPLES INTO IPOPULATION
		// TODO: yet to be tested
		try {
			df.buildSamples();
		} catch (final RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final InvalidFileTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		GosplHierarchicalSampler hs = new GosplHierarchicalSampler();
		
		hs.setDistributions(df.getRawDistributions());
		
		/*
		// BUILD THE SAMPLER WITH THE INFERENCE ALGORITHM
		final IDistributionInferenceAlgo<ASurveyAttribute, AValue> distributionInfAlgo =
				new HierarchicalHypothesisAlgo();
		

		ISampler<ACoordinate<ASurveyAttribute, AValue>> sampler = null;
		try {
			sampler = distributionInfAlgo.inferDistributionsSampler(
					df.getRawDistributions(), 
					this.getSamplerToTest()
					);

		} catch (final IllegalDistributionCreation e1) {
			e1.printStackTrace();
		}
		*/

		
		
	}

}
