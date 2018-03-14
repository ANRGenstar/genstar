package gospl.generator;

import core.metamodel.entity.ADemoEntity;
import gospl.GosplPopulation;
import gospl.sampler.ISampler;
import gospl.sampler.co.CombinatorialOptimizationSampler;
import gospl.sampler.co.UniformSampler;

/**
 * Generator based on sample based growth methods: these can either calls
 * optimization process or not that depend on the sampler type.
 * <p>
 * {@code Gospl} provides {@link UniformSampler} and {@link CombinatorialOptimizationSampler}
 * 
 * @author kevinchapuis
 *
 */
public class SampleBasedGenerator implements ISyntheticGosplPopGenerator {

	private ISampler<ADemoEntity> sampler;
	
	public SampleBasedGenerator(ISampler<ADemoEntity> sampler) {
		this.sampler = sampler;
	}
	
	@Override
	public GosplPopulation generate(int numberOfIndividual) {
		return new GosplPopulation(sampler.draw(numberOfIndividual));
	}

}
