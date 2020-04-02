package gospl.algo.co;

import java.util.Map;

import core.metamodel.IPopulation;
import core.metamodel.attribute.Attribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.value.IValue;
import gospl.sampler.ISampler;
import gospl.sampler.multilayer.co.ICOMultiLayerSampler;

public class MultiLayerSampleBasedAlgorithm<M extends ICOMultiLayerSampler> implements ICombinatorialOptimizationAlgo<M> {

	@Override
	public ISampler<ADemoEntity> setupCOSampler(IPopulation<ADemoEntity, Attribute<? extends IValue>> sample,
			boolean withWeights, M sampler) {
		sampler.setSample(sample,withWeights);
		return sampler;
	}
	
	public ISampler<ADemoEntity> setupCOSampler(int layer,
			Map<Integer,IPopulation<ADemoEntity, Attribute<? extends IValue>>> samples,
			boolean withWeights, M sampler) {
		sampler.setSample(samples,withWeights,layer);
		return sampler;
	}

}
