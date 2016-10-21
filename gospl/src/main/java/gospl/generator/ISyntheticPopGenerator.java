package gospl.generator;

import gospl.algos.exception.GosplSamplerException;
import io.metamodel.IPopulation;

public interface ISyntheticPopGenerator {

	public IPopulation generate(int numberOfIndividual) throws GosplSamplerException;
	
}
