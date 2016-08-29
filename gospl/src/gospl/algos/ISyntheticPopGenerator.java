package gospl.algos;

import gospl.algos.exception.GosplSampleException;
import gospl.metamodel.IPopulation;

public interface ISyntheticPopGenerator {

	public IPopulation generate(int numberOfIndividual) throws GosplSampleException;
	
}
