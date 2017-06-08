package gospl.algo.generator;

import core.metamodel.pop.APopulationEntity;
import gospl.GosplPopulation;
import gospl.algo.sampler.ISampler;
import gospl.algo.sampler.co.RandomSampler;
import gospl.algo.sampler.co.TabuSampler;

/**
 * Generator based on sample based growth methods: these can either calls
 * optimization process or not that depend on the sampler type.
 * <p>
 * {@code Gospl} provides {@link RandomSampler} and {@link TabuSampler}
 * <p>
 * WARNING: this generator does not guarantee that resulted population
 * will not contain duplicate entities
 * 
 * @author kevinchapuis
 *
 */
public class SampleBasedGenerator implements ISyntheticGosplPopGenerator {

	private ISampler<APopulationEntity> sampler;
	
	public SampleBasedGenerator(ISampler<APopulationEntity> sampler) {
		this.sampler = sampler;
	}
	
	@Override
	public GosplPopulation generate(int numberOfIndividual) {
		return new GosplPopulation(sampler.draw(numberOfIndividual));
	}

}
