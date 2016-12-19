package gospl.algo.sampler;

import gospl.algo.HierarchicalHypothesisAlgo;
import gospl.algo.ISyntheticReconstructionAlgo;
import gospl.algo.sampler.sr.GosplHierarchicalSampler;

public class TestHierarchicalSampler extends AbstractTestBasedOnRouenCase<IHierarchicalSampler> {


	@Override
	protected ISyntheticReconstructionAlgo<IHierarchicalSampler> getInferenceAlgoToTest() {
		return new HierarchicalHypothesisAlgo();
	}
	
	@Override
	protected IHierarchicalSampler getSamplerToTest() {
		return new GosplHierarchicalSampler();
	}

}
