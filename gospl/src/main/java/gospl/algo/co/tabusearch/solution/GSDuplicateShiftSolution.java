package gospl.algo.co.tabusearch.solution;

import java.util.Collection;
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

/**
 * Solution to tabu search in form of a {@link IPopulation}
 * <p>
 * The encapsulated population in this solution can contain duplicate entities
 * 
 * @author kevinchapuis
 *
 */
public class GSDuplicateShiftSolution extends AGSSampleBasedCOSolution {

	/**
	 * 
	 * @param population
	 * @param sample
	 */
	public GSDuplicateShiftSolution(IPopulation<ADemoEntity, DemographicAttribute<? extends IValue>> population,
			Collection<ADemoEntity> sample){
		super(population, sample);
	}

	/**
	 * 
	 * @param population
	 * @param sample
	 */
	public GSDuplicateShiftSolution(Collection<ADemoEntity> population, Collection<ADemoEntity> sample){
		super(population, sample);
	}


	// ---------------- State Transition management ---------------- //


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
		IPopulation<ADemoEntity, DemographicAttribute<? extends IValue>> newPopulation = new GosplPopulation(population);
		for(IValue value : valueList.stream().unordered().
				skip(dimensionalShiftNumber > valueList.size() ? 
						0 : valueList.size() - dimensionalShiftNumber)
				.collect(Collectors.toList())){
			Map<ADemoEntity, ADemoEntity> removeAddPair = this.findAnyTargetRemoveAddPair(
					newPopulation, value);
			if(removeAddPair.isEmpty())
				continue;
			ADemoEntity oldEntity = removeAddPair.keySet().iterator().next();
			newPopulation = super.deepSwitch(newPopulation, oldEntity, removeAddPair.get(oldEntity));
		}
		return new GSDuplicateShiftSolution(newPopulation, sample);
	}
	
	// ---------------- inner utility method ---------------- //
	
	private GSDuplicateShiftSolution getNeighbor(IValue value){
		if(!valueList.contains(value))
			throw new RuntimeException();
		Map<ADemoEntity, ADemoEntity> removeAddPair = super.findAnyTargetRemoveAddPair(
				this.population, value);
		if(removeAddPair.isEmpty())
			return null;
		ADemoEntity oldEntity = removeAddPair.keySet().iterator().next();
		return new GSDuplicateShiftSolution(super.deepSwitch(new GosplPopulation(this.population), 
				oldEntity, removeAddPair.get(oldEntity)), sample);
	}
	
}
