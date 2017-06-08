package gospl.algo.sb;

import core.metamodel.IPopulation;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;
import gospl.algo.ICombinatorialOptimizationAlgo;
import gospl.algo.sampler.IEntitySampler;
import gospl.algo.sampler.ISampler;

public class SampleBasedAlgorithm implements ICombinatorialOptimizationAlgo<IEntitySampler> {

	@Override
	public ISampler<APopulationEntity> setupCOSampler(
			IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> sample, 
			IEntitySampler sampler) {
		sampler.setSample(sample);
		return sampler;
	}

}
