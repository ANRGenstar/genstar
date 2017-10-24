package gospl.algo.co;

import core.metamodel.IPopulation;
import core.metamodel.pop.ADemoEntity;
import core.metamodel.pop.attribute.DemographicAttribute;
import core.metamodel.value.IValue;
import gospl.sampler.ISampler;

public interface ICombinatorialOptimizationAlgo<SamplerType extends ISampler<ADemoEntity>> {

	/**
	 * This method must provide a way to build a Combinatorial Optimization (CO) sampler. CO is known in the literature
	 * as the method to generate synthetic population growing a population sample using optimization algorithm
	 * 
	 * @param sample
	 * @param sampler
	 * @return
	 */
	public ISampler<ADemoEntity> setupCOSampler(
			IPopulation<ADemoEntity, DemographicAttribute<? extends IValue>> sample,
			SamplerType sampler);
	
}
