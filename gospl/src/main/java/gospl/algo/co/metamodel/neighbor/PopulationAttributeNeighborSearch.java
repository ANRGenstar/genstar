package gospl.algo.co.metamodel.neighbor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

import core.metamodel.IPopulation;
import core.metamodel.attribute.Attribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.entity.comparator.HammingEntityComparator;
import core.metamodel.value.IValue;
import core.util.random.GenstarRandomUtils;
import gospl.GosplPopulation;

public class PopulationAttributeNeighborSearch implements IPopulationNeighborSearch<Attribute<? extends IValue>> {
	
	private Collection<Attribute<? extends IValue>> predicates;
	private IPopulation<ADemoEntity, Attribute<? extends IValue>> sample;
	
	public PopulationAttributeNeighborSearch() {
		this.predicates = new HashSet<>();
	}
	
	// ------------------------ NEIGHBORING ------------------------ //

	@Override
	public IPopulation<ADemoEntity, Attribute<? extends IValue>> getNeighbor(
			IPopulation<ADemoEntity, Attribute<? extends IValue>> population, 
			Attribute<? extends IValue> predicate, int degree) {
		// If predicate does not concern this population
		if(!population.hasPopulationAttributeNamed(predicate.getAttributeName()))
			throw new IllegalArgumentException("Trying to search for neighbor population on attribute "
					+predicate.getAttributeName()+" that is not present");
		
		Map<ADemoEntity, ADemoEntity> removeAddPair = this.findPairedTarget(population, predicate);
		
		ADemoEntity oldEntity = removeAddPair.keySet().iterator().next();
		ADemoEntity newEntity = population.contains(removeAddPair.get(oldEntity)) ? 
				(ADemoEntity) removeAddPair.get(oldEntity).clone() : removeAddPair.get(oldEntity);
				
		return IPopulationNeighborSearch.deepSwitch(new GosplPopulation(population), oldEntity, newEntity);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * WARNING: Can be very time consuming, because it has to check for an entity in the sample that has the following
	 * property: have the exact same set of value except for predicate attribute. If none have been found, rely on hamming
	 * distance with predicate attribute regex.
	 * <p>
	 * @see HammingEntityComparator
	 */
	@Override
	public Map<ADemoEntity, ADemoEntity> findPairedTarget(
			IPopulation<ADemoEntity, Attribute<? extends IValue>> population, 
			Attribute<? extends IValue> predicate) {
		Map<ADemoEntity, ADemoEntity> pair = new HashMap<>();
		
		ADemoEntity oldEntity = GenstarRandomUtils.oneOf(population);
		IValue target = oldEntity.getValueForAttribute(predicate);
		Collection<IValue> matches = new ArrayList<>(oldEntity.getValues());
		matches.remove(target);
		
		Optional<ADemoEntity> candidateEntity = sample.stream().filter(e -> 
			!e.getValueForAttribute(predicate).equals(oldEntity.getValueForAttribute(predicate)) 
				&& e.getValues().containsAll(matches)).findFirst();
		if(candidateEntity.isPresent())
			pair.put(oldEntity, candidateEntity.get());
		else {
			pair.put(oldEntity, this.sample.stream().filter(e -> 
				!e.getValueForAttribute(predicate).equals(oldEntity.getValueForAttribute(predicate)))
				.sorted(new HammingEntityComparator(oldEntity)).findFirst().get());
		}
		return pair;
	}
	
	// ---------------------------------------------------- //

	@Override
	public Collection<Attribute<? extends IValue>> getPredicates() {
		return Collections.unmodifiableCollection(this.predicates);
	}
	
	@Override
	public void setPredicates(Collection<Attribute<? extends IValue>> predicates) {
		this.predicates = predicates;
	}

	@Override
	public void addPredicates(Attribute<? extends IValue> predicate) {
		this.predicates.add(predicate);
	}

	@Override
	public void setSample(IPopulation<ADemoEntity, Attribute<? extends IValue>> sample) {
		this.sample = sample;
	}
	
}
