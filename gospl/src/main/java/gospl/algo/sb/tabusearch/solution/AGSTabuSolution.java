package gospl.algo.sb.tabusearch.solution;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import core.metamodel.IPopulation;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;
import gospl.GosplPopulation;
import gospl.algo.sb.metamodel.AGSSampleBasedCOSolution;
import gospl.algo.sb.metamodel.IGSSampleBasedCOSolution;

public abstract class AGSTabuSolution extends AGSSampleBasedCOSolution {

	public AGSTabuSolution(IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> population,
			Collection<APopulationEntity> sample){
		super(population, sample);
	}
	
	public AGSTabuSolution(Collection<APopulationEntity> population, Collection<APopulationEntity> sample) {
		super(population, sample);
	}
	
	// ------------------- utility methods ------------------- //
	
	protected Map<APopulationEntity, APopulationEntity> findAnyTargetRemoveAddPair(APopulationValue value){
		if(!valueList.contains(value))
			throw new RuntimeException();
		Map<APopulationEntity, Collection<APopulationValue>> expectedRemove = population.stream()
				.filter(entity -> entity.getValues().contains(value))
				.collect(Collectors.toSet()).stream()
				.collect(Collectors.toMap(Function.identity(), 
						entity -> entity.getValues().stream().filter(val -> !val.equals(value))
						.collect(Collectors.toList())));
		Optional<APopulationEntity> newEntity = sample.stream().filter(entity -> expectedRemove.values()
				.stream().anyMatch(values -> entity.getValues().containsAll(values)))
				.findFirst();
		if(newEntity.isPresent()){
			APopulationEntity oldEntity = expectedRemove.keySet().stream().filter(entity -> newEntity.get().getValues()
					.containsAll(expectedRemove.get(entity)))
					.findFirst().get();
			return Stream.of(oldEntity).collect(Collectors.toMap(Function.identity(), e -> newEntity.get()));
		}
		return Collections.emptyMap();
	}
	
	protected IGSSampleBasedCOSolution getNeighbor(APopulationValue value, boolean duplicate){
		if(!valueList.contains(value))
			throw new RuntimeException();
		Map<APopulationEntity, APopulationEntity> removeAddPair = this.findAnyTargetRemoveAddPair(value);
		if(removeAddPair.isEmpty())
			return null;
		APopulationEntity oldEntity = removeAddPair.keySet().iterator().next();
		APopulationEntity newEntity = !duplicate &&
				population.contains(removeAddPair.get(oldEntity)) ? 
						removeAddPair.get(oldEntity).clone() : removeAddPair.get(oldEntity);
		IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> newPopulation = new GosplPopulation(population);
		if(!newPopulation.remove(oldEntity) || !newPopulation.add(newEntity))
				throw new RuntimeException("Encounter a problem while switching between two entities:\n"
						+ "remove entity = "+oldEntity.toString()+"\n"
						+ "new entity = "+removeAddPair.get(oldEntity).toString());
		return new GSDuplicateShiftSolution(newPopulation, sample);
	}

}
