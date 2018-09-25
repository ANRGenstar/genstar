package gospl.algo.sampler.sr;

import gospl.generator.util.GSUtilGenerator;
import gospl.generator.util.GSUtilPopulation;

public class SamplerTestSetup {

	int popSize = (int) Math.pow(10, 4);
	GSUtilPopulation uPop;
	
	public SamplerTestSetup() {
		uPop = new GSUtilPopulation(new GSUtilGenerator(2, 5).generate(popSize/100));
	} 
	
}
