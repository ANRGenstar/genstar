package gospl.algo.co;

import core.metamodel.IPopulation;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;
import gospl.sampler.ISampler;

public interface ICombinatorialOptimizationAlgo<SamplerType extends ISampler<APopulationEntity>> {

	/**
	 * This method must provide a way to build a Combinatorial Optimization (CO) sampler. CO is known in the literature
	 * as the method to generate synthetic population growing a population sample using optimization algorithm
	 * 
	 * @param sample
	 * @param sampler
	 * @return
	 */
	public ISampler<APopulationEntity> setupCOSampler(
			IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> sample,
			SamplerType sampler);
	
}
