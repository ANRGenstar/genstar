package gospl.algo.co.metamodel.neighbor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import core.metamodel.IPopulation;
import core.metamodel.attribute.Attribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.entity.comparator.HammingEntityComparator;
import core.metamodel.value.IValue;
import core.util.random.GenstarRandomUtils;

/**
 * Will search for neighbor based on entity as predicate: meaning that basic search will swap a given
 * entity with another one from a sample.
 * 
 * @author kevinchapuis
 *
 */
public class PopulationEntityNeighborSearch implements IPopulationNeighborSearch<ADemoEntity> {

	private IPopulation<ADemoEntity, Attribute<? extends IValue>> sample;
	private Collection<ADemoEntity> predicates;
	
	public PopulationEntityNeighborSearch() {
		this.predicates = new HashSet<>();
	}
	
	// ---------------------------------------------- //

	@Override
	public Map<ADemoEntity, ADemoEntity> getPairwisedEntities(
			IPopulation<ADemoEntity, Attribute<? extends IValue>> population, 
			ADemoEntity predicate, int size) {
		
		Map<ADemoEntity, ADemoEntity> pair = new HashMap<>();
		
		Set<ADemoEntity> predicates = new HashSet<>(Arrays.asList(predicate));
		if(size > 1)
			predicates = population.stream().sorted(new HammingEntityComparator(predicate))
				.limit(size).collect(Collectors.toSet());
		
		for(ADemoEntity oldEntity : predicates) {
			ADemoEntity candidateEntity = GenstarRandomUtils.oneOf(sample);
			while(candidateEntity.equals(oldEntity))
				candidateEntity = GenstarRandomUtils.oneOf(sample);
			pair.put(oldEntity, candidateEntity);
		}
		
		return pair;
	}

	@Override
	public Collection<ADemoEntity> getPredicates() {
		return Collections.unmodifiableCollection(predicates);
	}
	
	@Override
	public void setPredicates(Collection<ADemoEntity> predicates) {
		this.predicates = predicates;
	}

	@Override
	public void updatePredicates(IPopulation<ADemoEntity, Attribute<? extends IValue>> population) {
		this.setPredicates(population);
	}

	@Override
	public void setSample(IPopulation<ADemoEntity, Attribute<? extends IValue>> sample) {
		this.sample = sample;
	}

}
