package gospl.generator;

import core.metamodel.pop.ADemoEntity;
import gospl.GosplPopulation;
import gospl.sampler.ISampler;
import gospl.sampler.co.RandomSampler;
import gospl.sampler.co.TabuSampler;

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

	private ISampler<ADemoEntity> sampler;
	
	public SampleBasedGenerator(ISampler<ADemoEntity> sampler) {
		this.sampler = sampler;
	}
	
	@Override
	public GosplPopulation generate(int numberOfIndividual) {
		return new GosplPopulation(sampler.draw(numberOfIndividual));
	}

}
