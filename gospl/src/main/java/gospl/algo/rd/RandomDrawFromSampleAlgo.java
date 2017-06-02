package gospl.algo.rd;

import core.metamodel.IPopulation;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;
import gospl.algo.ICombinatorialOptimizationAlgo;
import gospl.algo.sampler.ISampler;
import gospl.algo.sampler.co.RandomSampler;

public class RandomDrawFromSampleAlgo implements ICombinatorialOptimizationAlgo<RandomSampler> {

	@Override
	public ISampler<APopulationEntity> inferCOSampler(
			IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> sample, RandomSampler sampler) {
		sampler.setSample(sample);
		return sampler;
	}

}
