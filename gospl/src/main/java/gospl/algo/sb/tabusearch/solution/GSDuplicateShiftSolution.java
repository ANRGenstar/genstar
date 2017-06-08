package gospl.algo.sb.tabusearch.solution;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import core.metamodel.IPopulation;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;
import core.util.random.GenstarRandom;
import gospl.GosplPopulation;
import gospl.algo.sb.metamodel.IGSSampleBasedCOSolution;

/**
 * Solution to tabu search in form of a {@link IPopulation}
 * <p>
 * The encapsulated population in this solution can contain duplicate entities
 * 
 * @author kevinchapuis
 *
 */
public class GSDuplicateShiftSolution extends AGSTabuSolution {

	/**
	 * 
	 * @param population
	 * @param sample
	 */
	public GSDuplicateShiftSolution(IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> population,
			Collection<APopulationEntity> sample){
		super(population, sample);
	}

	/**
	 * 
	 * @param population
	 * @param sample
	 */
	public GSDuplicateShiftSolution(Collection<APopulationEntity> population, Collection<APopulationEntity> sample){
		super(population, sample);
	}


	// ---------------- State Transition management ---------------- //


	@Override
	public Collection<IGSSampleBasedCOSolution> getNeighbors() {
		return valueList.stream().map(value -> super.getNeighbor(value, true))
				.filter(solution -> solution != null)
				.collect(Collectors.toList());
	}

	@Override
	public IGSSampleBasedCOSolution getRandomNeighbor() {
		IGSSampleBasedCOSolution neighbor = null;
		while(neighbor == null)
			neighbor = super.getNeighbor(valueList.stream().skip(GenstarRandom
					.getInstance().nextInt(valueList.size())).findFirst().get(), true);
		return neighbor;
	}

	@Override
	public IGSSampleBasedCOSolution getRandomNeighbor(int dimensionalShiftNumber) {
		IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> newPopulation = new GosplPopulation(population);
		for(APopulationValue value : valueList.stream().unordered().
				skip(dimensionalShiftNumber > valueList.size() ? 
						0 : valueList.size() - dimensionalShiftNumber)
				.collect(Collectors.toList())){
			Map<APopulationEntity, APopulationEntity> removeAddPair = this.findAnyTargetRemoveAddPair(value);
			if(removeAddPair.isEmpty())
				continue;
			APopulationEntity oldEntity = removeAddPair.keySet().iterator().next();
			if(!newPopulation.remove(oldEntity) 
					|| !newPopulation.add(removeAddPair.get(oldEntity)))
					throw new RuntimeException("Encounter a problem while switching between two entities:\n"
							+ "remove entity = "+oldEntity.toString()+"\n"
							+ "new entity = "+removeAddPair.get(oldEntity).toString());
		}
		return new GSDuplicateShiftSolution(newPopulation, sample);
	}
	
}
