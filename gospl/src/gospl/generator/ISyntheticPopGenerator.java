package gospl.generator;

import gospl.algos.exception.GosplSamplerException;
import gospl.metamodel.IPopulation;

public interface ISyntheticPopGenerator {

	public IPopulation generate(int numberOfIndividual) throws GosplSamplerException;
	
}
