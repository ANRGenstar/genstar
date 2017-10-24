package gospl.algo.co.simannealing.state;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import core.metamodel.IPopulation;
import core.metamodel.pop.ADemoEntity;
import core.metamodel.pop.attribute.DemographicAttribute;
import core.metamodel.value.IValue;
import core.util.random.GenstarRandom;
import gospl.GosplPopulation;
import gospl.algo.co.metamodel.AGSSampleBasedCOSolution;
import gospl.algo.co.metamodel.IGSSampleBasedCOSolution;

public class GSSAState extends AGSSampleBasedCOSolution {

	public GSSAState(IPopulation<ADemoEntity, DemographicAttribute<? extends IValue>> population,
			Collection<ADemoEntity> sample){
		super(population, sample);
	}

	public GSSAState(Collection<ADemoEntity> population, Collection<ADemoEntity> sample) {
		super(population, sample);
	}

	// ----------------- State Transition methods ----------------- //

	@Override
	public IGSSampleBasedCOSolution getRandomNeighbor() {
		return getRandomNeighbor(1);
	}

	@Override
	public IGSSampleBasedCOSolution getRandomNeighbor(int dimensionalShiftNumber) {
		List<IValue> popShift = valueList.stream().skip(GenstarRandom.getInstance().nextInt(
				valueList.size() < dimensionalShiftNumber ? valueList.size() : dimensionalShiftNumber))
				.collect(Collectors.toList());
		IPopulation<ADemoEntity, DemographicAttribute<? extends IValue>> newPop = new GosplPopulation(population);
		for(IValue value : popShift){
			Map<ADemoEntity, ADemoEntity> removeAddPair = super.findAnyTargetRemoveAddPair(
					newPop, value);
			ADemoEntity oldEntity = removeAddPair.keySet().iterator().next();
			ADemoEntity newEntity = newPop.contains(removeAddPair.get(oldEntity)) ?
					(ADemoEntity) removeAddPair.get(oldEntity).clone() : removeAddPair.get(oldEntity);
			newPop = super.deepSwitch(newPop, oldEntity, newEntity);
		}
		return new GSSAState(newPop, super.sample);
	}

	@Override
	public Collection<IGSSampleBasedCOSolution> getNeighbors() {
		Collection<IGSSampleBasedCOSolution> neighbors = new ArrayList<>();
		for(IValue value : valueList){
			Map<ADemoEntity, ADemoEntity> removeAddPair = super.findAnyTargetRemoveAddPair(
					super.population, value);
			ADemoEntity oldEntity = removeAddPair.keySet().iterator().next();
			ADemoEntity newEntity = super.population.contains(removeAddPair.get(oldEntity)) ?
					(ADemoEntity) removeAddPair.get(oldEntity).clone() : removeAddPair.get(oldEntity);
					neighbors.add(new GSSAState(super.deepSwitch(new GosplPopulation(super.population), oldEntity, newEntity),
							super.sample));
		}
		return null;
	}

}
