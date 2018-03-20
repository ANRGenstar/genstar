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

/**
 * Will search for neighbor based on attribute as predicate: meaning that basic search will swap a given
 * entity with another one from a sample, based on the fact that they have same value attribute (or the
 * highest number of common value attribute) except the one given as predicate.
 * 
 * @author kevinchapuis
 *
 */
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

		IPopulation<ADemoEntity, Attribute<? extends IValue>> neighbor = new GosplPopulation(population);
		IPopulation<ADemoEntity, Attribute<? extends IValue>> buffer = new GosplPopulation(population);

		for(int i = 0; i < degree; i++) {
			Map<ADemoEntity, ADemoEntity> removeAddPair = this.findPairedTarget(buffer, predicate);

			ADemoEntity oldEntity = removeAddPair.keySet().iterator().next();
			ADemoEntity newEntity = removeAddPair.get(oldEntity).clone();

			neighbor = IPopulationNeighborSearch.deepSwitch(neighbor, oldEntity, newEntity);
			buffer.remove(oldEntity);
		}
		
		return neighbor;
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
	public void updatePredicates(IPopulation<ADemoEntity, Attribute<? extends IValue>> population) {
		this.setPredicates(population.getPopulationAttributes());
	}

	@Override
	public void setSample(IPopulation<ADemoEntity, Attribute<? extends IValue>> sample) {
		this.sample = sample;
	}

}
