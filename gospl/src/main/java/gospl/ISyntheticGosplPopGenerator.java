package gospl;

import gospl.algos.exception.GosplSamplerException;
import gospl.metamodel.GosplPopulation;

public interface ISyntheticGosplPopGenerator {

	public GosplPopulation generate(int numberOfIndividual) throws GosplSamplerException;
	
}
