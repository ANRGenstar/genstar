package gospl.algo.co.simannealing.state;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import core.metamodel.IPopulation;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;
import core.util.random.GenstarRandom;
import gospl.GosplPopulation;
import gospl.algo.co.metamodel.AGSSampleBasedCOSolution;
import gospl.algo.co.metamodel.IGSSampleBasedCOSolution;

public class GSSAState extends AGSSampleBasedCOSolution {

	public GSSAState(IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> population,
			Collection<APopulationEntity> sample){
		super(population, sample);
	}

	public GSSAState(Collection<APopulationEntity> population, Collection<APopulationEntity> sample) {
		super(population, sample);
	}

	// ----------------- State Transition methods ----------------- //

	@Override
	public IGSSampleBasedCOSolution getRandomNeighbor() {
		return getRandomNeighbor(1);
	}

	@Override
	public IGSSampleBasedCOSolution getRandomNeighbor(int dimensionalShiftNumber) {
		List<APopulationValue> popShift = valueList.stream().skip(GenstarRandom.getInstance().nextInt(
				valueList.size() < dimensionalShiftNumber ? valueList.size() : dimensionalShiftNumber))
				.collect(Collectors.toList());
		IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> newPop = new GosplPopulation(population);
		for(APopulationValue value : popShift){
			Map<APopulationEntity, APopulationEntity> removeAddPair = super.findAnyTargetRemoveAddPair(
					newPop, value);
			APopulationEntity oldEntity = removeAddPair.keySet().iterator().next();
			APopulationEntity newEntity = newPop.contains(removeAddPair.get(oldEntity)) ?
					(APopulationEntity) removeAddPair.get(oldEntity).clone() : removeAddPair.get(oldEntity);
			newPop = super.deepSwitch(newPop, oldEntity, newEntity);
		}
		return new GSSAState(newPop, super.sample);
	}

	@Override
	public Collection<IGSSampleBasedCOSolution> getNeighbors() {
		Collection<IGSSampleBasedCOSolution> neighbors = new ArrayList<>();
		for(APopulationValue value : valueList){
			Map<APopulationEntity, APopulationEntity> removeAddPair = super.findAnyTargetRemoveAddPair(
					super.population, value);
			APopulationEntity oldEntity = removeAddPair.keySet().iterator().next();
			APopulationEntity newEntity = super.population.contains(removeAddPair.get(oldEntity)) ?
					(APopulationEntity) removeAddPair.get(oldEntity).clone() : removeAddPair.get(oldEntity);
					neighbors.add(new GSSAState(super.deepSwitch(new GosplPopulation(super.population), oldEntity, newEntity),
							super.sample));
		}
		return null;
	}

}
