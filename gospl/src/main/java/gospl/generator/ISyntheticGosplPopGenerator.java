package gospl.generator;

import gospl.metamodel.GosplPopulation;

public interface ISyntheticGosplPopGenerator {

	public GosplPopulation generate(int numberOfIndividual);
	
}
