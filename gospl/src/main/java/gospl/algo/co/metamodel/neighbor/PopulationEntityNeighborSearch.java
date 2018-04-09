package gospl.algo.co.metamodel.neighbor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import core.metamodel.IPopulation;
import core.metamodel.attribute.Attribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.entity.comparator.HammingEntityComparator;
import core.metamodel.value.IValue;
import core.util.random.GenstarRandomUtils;
import gospl.GosplPopulation;

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

	/**
	 * {@inheritDoc}
	 * <p>
	 * If degree is more than 1, then close entities are chosen to be swap. Closeness is define
	 * by Hamming distance between entities @see {@link HammingEntityComparator}
	 * 
	 */
	@Override
	public IPopulation<ADemoEntity, Attribute<? extends IValue>> getNeighbor(
			IPopulation<ADemoEntity, Attribute<? extends IValue>> population, ADemoEntity predicate, int degree) {
		
		Set<ADemoEntity> predicates = new HashSet<>(Arrays.asList(predicate));
		if(degree > 1)
			predicates = population.stream().sorted(new HammingEntityComparator(predicate))
				.limit(degree).collect(Collectors.toSet());
		
		IPopulation<ADemoEntity, Attribute<? extends IValue>> neighbor = new GosplPopulation(population);
		
		for(ADemoEntity u : predicates) {
			ADemoEntity[] pair = findPairedTarget(neighbor, u);
			neighbor = IPopulationNeighborSearch.deepSwitch(neighbor, pair[0], pair[1].clone());
		}
		
		return neighbor;
	}

	@Override
	public ADemoEntity[] findPairedTarget(
			IPopulation<ADemoEntity, Attribute<? extends IValue>> population, ADemoEntity predicate) {
		ADemoEntity candidateEntity = GenstarRandomUtils.oneOf(sample);
		while(candidateEntity.equals(predicate))
			candidateEntity = GenstarRandomUtils.oneOf(sample);
		return new ADemoEntity[] {predicate, candidateEntity};
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
