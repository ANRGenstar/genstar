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

}
