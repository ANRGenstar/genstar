package gospl.algo.sb.simannealing.state;

import java.util.Collection;

import core.metamodel.IPopulation;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;
import gospl.algo.sb.metamodel.AGSSampleBasedCOSolution;
import gospl.algo.sb.metamodel.IGSSampleBasedCOSolution;

public class GSSAState extends AGSSampleBasedCOSolution {

	public GSSAState(IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> population,
			Collection<APopulationEntity> sample){
		super(population, sample);
	}
	
	public GSSAState(Collection<APopulationEntity> population, Collection<APopulationEntity> sample) {
		super(population, sample);
	}

	@Override
	public IGSSampleBasedCOSolution getRandomNeighbor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IGSSampleBasedCOSolution getRandomNeighbor(int dimensionalShiftNumber) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<IGSSampleBasedCOSolution> getNeighbors() {
		// TODO Auto-generated method stub
		return null;
	}

}
