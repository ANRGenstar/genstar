package gospl.algo;

import core.metamodel.IPopulation;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;
import gospl.algo.sampler.ISampler;

public interface ICombinatorialOptimizationAlgo<SamplerType extends ISampler<APopulationEntity>> {

	public ISampler<APopulationEntity> inferCOSampler(
			IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> sample, 
			SamplerType sampler);
	
}
