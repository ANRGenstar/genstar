package gospl.algo.sampler;

import gospl.algo.sr.ISyntheticReconstructionAlgo;
import gospl.algo.sr.hs.HierarchicalHypothesisAlgo;
import gospl.sampler.IHierarchicalSampler;
import gospl.sampler.sr.GosplHierarchicalSampler;

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
