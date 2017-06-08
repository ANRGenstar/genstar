package gospl.algo.sb.metamodel;

import java.util.Collection;
import java.util.Set;

import core.metamodel.IPopulation;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;
import gospl.distribution.matrix.AFullNDimensionalMatrix;

public interface IGSSampleBasedCOSolution {

	public IGSSampleBasedCOSolution getRandomNeighbor();
	
	public IGSSampleBasedCOSolution getRandomNeighbor(int dimensionalShiftNumber);
	
	public Collection<IGSSampleBasedCOSolution> getNeighbors();

	public Double getFitness(Set<AFullNDimensionalMatrix<Integer>> objectives);
	
	public IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> getSolution();
	
}
