package gospl.algo.sampler;

import gospl.algo.HierarchicalHypothesisAlgo;
import gospl.algo.IDistributionInferenceAlgo;
import gospl.algo.sampler.sr.GosplHierarchicalSampler;

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
