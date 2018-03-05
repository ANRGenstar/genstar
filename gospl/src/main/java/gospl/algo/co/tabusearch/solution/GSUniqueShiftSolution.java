package gospl.algo.co.tabusearch.solution;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import core.metamodel.IPopulation;
import core.metamodel.attribute.Attribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.value.IValue;
import core.util.random.GenstarRandom;
import gospl.GosplPopulation;
import gospl.algo.co.metamodel.AGSSampleBasedCOSolution;
import gospl.algo.co.metamodel.IGSSampleBasedCOSolution;

/**
 * 
 * @author kevinchapuis
 *
 */
public class GSUniqueShiftSolution extends AGSSampleBasedCOSolution {
	
	/**
	 * 
	 * @param population
	 * @param sample
	 */
	public GSUniqueShiftSolution(IPopulation<ADemoEntity, Attribute<? extends IValue>> population,
			Collection<ADemoEntity> sample){
		super(population, sample);
	}
	
	/**
	 * 
	 * @param population
	 * @param sample
	 */
	public GSUniqueShiftSolution(Set<ADemoEntity> population, Collection<ADemoEntity> sample){
		super(population, sample);
	}
	
	
	// ----------------- State Transition Management ----------------- //
	

	@Override
	public Collection<IGSSampleBasedCOSolution> getNeighbors() {
		return valueList.stream().map(value -> this.getNeighbor(value))
				.filter(solution -> solution != null)
				.collect(Collectors.toList());
	}

	@Override
	public IGSSampleBasedCOSolution getRandomNeighbor() {
		IGSSampleBasedCOSolution neighbor = null;
		while(neighbor == null)
			neighbor = this.getNeighbor(valueList.stream().skip(GenstarRandom
					.getInstance().nextInt(valueList.size())).findFirst().get());
		return neighbor;
	}

	@Override
	public IGSSampleBasedCOSolution getRandomNeighbor(int dimensionalShiftNumber) {
		IPopulation<ADemoEntity, Attribute<? extends IValue>> newPopulation = new GosplPopulation(population);
		for(IValue value : valueList.stream().unordered().
				skip(dimensionalShiftNumber > valueList.size() ? 
						0 : valueList.size() - dimensionalShiftNumber)
				.collect(Collectors.toList())){
			Map<ADemoEntity, ADemoEntity> removeAddPair = this.findAnyTargetRemoveAddPair(
					newPopulation, value);
			if(removeAddPair.isEmpty())
				continue;
			ADemoEntity oldEntity = removeAddPair.keySet().iterator().next();
			ADemoEntity newEntity = newPopulation.contains(removeAddPair.get(oldEntity)) ?
					(ADemoEntity) removeAddPair.get(oldEntity).clone() : removeAddPair.get(oldEntity);
			newPopulation = super.deepSwitch(newPopulation, oldEntity, newEntity);
		}
		return new GSDuplicateShiftSolution(newPopulation, sample);
	}

	// ---------------- inner utility methods ---------------- //
	
	private GSUniqueShiftSolution getNeighbor(IValue value){
		if(!valueList.contains(value))
			throw new RuntimeException();
		Map<ADemoEntity, ADemoEntity> removeAddPair = super.findAnyTargetRemoveAddPair(
				this.population, value);
		if(removeAddPair.isEmpty())
			return null;
		ADemoEntity oldEntity = removeAddPair.keySet().iterator().next();
		ADemoEntity newEntity = this.population.contains(removeAddPair.get(oldEntity)) ? 
						(ADemoEntity) removeAddPair.get(oldEntity).clone() : removeAddPair.get(oldEntity);
		return new GSUniqueShiftSolution(super.deepSwitch(new GosplPopulation(this.population), 
				oldEntity, newEntity), sample);
	}


}
