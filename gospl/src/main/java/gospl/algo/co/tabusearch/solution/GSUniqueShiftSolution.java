package gospl.algo.co.tabusearch.solution;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import core.metamodel.IPopulation;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;
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
	public GSUniqueShiftSolution(IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> population,
			Collection<APopulationEntity> sample){
		super(population, sample);
	}
	
	/**
	 * 
	 * @param population
	 * @param sample
	 */
	public GSUniqueShiftSolution(Set<APopulationEntity> population, Collection<APopulationEntity> sample){
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
		IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> newPopulation = new GosplPopulation(population);
		for(APopulationValue value : valueList.stream().unordered().
				skip(dimensionalShiftNumber > valueList.size() ? 
						0 : valueList.size() - dimensionalShiftNumber)
				.collect(Collectors.toList())){
			Map<APopulationEntity, APopulationEntity> removeAddPair = this.findAnyTargetRemoveAddPair(
					newPopulation, value);
			if(removeAddPair.isEmpty())
				continue;
			APopulationEntity oldEntity = removeAddPair.keySet().iterator().next();
			APopulationEntity newEntity = newPopulation.contains(removeAddPair.get(oldEntity)) ?
					removeAddPair.get(oldEntity).clone() : removeAddPair.get(oldEntity);
			newPopulation = super.deepSwitch(newPopulation, oldEntity, newEntity);
		}
		return new GSDuplicateShiftSolution(newPopulation, sample);
	}

	// ---------------- inner utility methods ---------------- //
	
	private GSUniqueShiftSolution getNeighbor(APopulationValue value){
		if(!valueList.contains(value))
			throw new RuntimeException();
		Map<APopulationEntity, APopulationEntity> removeAddPair = super.findAnyTargetRemoveAddPair(
				this.population, value);
		if(removeAddPair.isEmpty())
			return null;
		APopulationEntity oldEntity = removeAddPair.keySet().iterator().next();
		APopulationEntity newEntity = this.population.contains(removeAddPair.get(oldEntity)) ? 
						removeAddPair.get(oldEntity).clone() : removeAddPair.get(oldEntity);
		return new GSUniqueShiftSolution(super.deepSwitch(new GosplPopulation(this.population), 
				oldEntity, newEntity), sample);
	}


}
