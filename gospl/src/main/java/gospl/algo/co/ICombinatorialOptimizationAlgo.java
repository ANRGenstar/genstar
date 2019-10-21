package gospl.algo.co;

import core.metamodel.IPopulation;
import core.metamodel.attribute.Attribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.value.IValue;
import gospl.sampler.IEntitySampler;
import gospl.sampler.ISampler;
import gospl.sampler.co.CombinatorialOptimizationSampler;

/**
 * Interface that defines combinatorial optimization general contract. This algorithm encapsulate
 * entity sampler - that {@link IEntitySampler} - which means that they will draw individual entity directly from
 * a sample.
 * <p>
 * Implementing class: {@link SampleBasedAlgorithm}
 * 
 * @author kevinchapuis
 *
 * @param <SamplerType> the type of sample. It should implement {@link IEntitySampler}
 * 
 * @see CombinatorialOptimizationSampler
 * @see IGosplConcept.EGosplGenerationConcept#CO
 */
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
			IPopulation<ADemoEntity, Attribute<? extends IValue>> sample,
			SamplerType sampler);
	
}
