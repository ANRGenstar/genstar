package gospl.algo.sampler.sr;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import gospl.GosplEntity;
import gospl.GosplPopulation;
import gospl.generator.util.GSUtilGenerator;
import gospl.generator.util.GSUtilPopulation;
import gospl.sampler.IDistributionSampler;

public class SamplerTestSetup<Sampler extends IDistributionSampler> {

	public static final int NBENTITY = 1000;
	
	private GSUtilPopulation uPop;
	private Sampler sampler;
	
	public SamplerTestSetup(Sampler sampler) {
		this.uPop = new GSUtilPopulation(new GSUtilGenerator(2, 5).generate(NBENTITY));
		this.sampler = sampler;
		this.sampler.setDistribution(uPop.getFrequency());
	} 
	
	public GSUtilPopulation getBasePopulationUtil() {
		return this.uPop;
	}
	
	public GosplPopulation drawPopulation(int nbDraw) {
		return new GosplPopulation(IntStream.range(0, nbDraw)
				.parallel()
				.mapToObj(i -> new GosplEntity(sampler.draw().getMap()))
				.collect(Collectors.toList()));
	}
	
}
