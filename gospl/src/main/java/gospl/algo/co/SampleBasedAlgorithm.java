package gospl.algo.co;

import core.metamodel.IPopulation;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;
import gospl.sampler.IEntitySampler;
import gospl.sampler.ISampler;

public class SampleBasedAlgorithm implements ICombinatorialOptimizationAlgo<IEntitySampler> {

	@Override
	public ISampler<APopulationEntity> setupCOSampler(
			IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> sample, 
			IEntitySampler sampler) {
		sampler.setSample(sample);
		return sampler;
	}

}
