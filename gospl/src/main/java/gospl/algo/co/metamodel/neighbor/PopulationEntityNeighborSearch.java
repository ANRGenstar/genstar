package gospl.algo.co.metamodel.neighbor;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import core.metamodel.IPopulation;
import core.metamodel.attribute.Attribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.entity.comparator.HammingEntityComparator;
import core.metamodel.value.IValue;
import core.util.random.GenstarRandomUtils;
import gospl.GosplPopulation;

public class PopulationEntityNeighborSearch implements IPopulationNeighborSearch<ADemoEntity> {

	private IPopulation<ADemoEntity, Attribute<? extends IValue>> sample;

	@Override
	public IPopulation<ADemoEntity, Attribute<? extends IValue>> getNeighbor(
			IPopulation<ADemoEntity, Attribute<? extends IValue>> population, ADemoEntity predicate, int degree) {
		
		List<ADemoEntity> predicates = sample.stream().sorted(new HammingEntityComparator(predicate))
				.limit(degree-1).collect(Collectors.toList());
		predicates.add(0, predicate);
		
		IPopulation<ADemoEntity, Attribute<? extends IValue>> neighbor = new GosplPopulation(population);
		for(ADemoEntity u : predicates) {
			Map<ADemoEntity, ADemoEntity> pair = findPairedTarget(population, u);
			ADemoEntity oldEntity = pair.keySet().iterator().next();
			ADemoEntity newEntity = population.contains(pair.get(oldEntity)) ? 
					(ADemoEntity) pair.get(oldEntity).clone() : pair.get(oldEntity);
					
			neighbor = IPopulationNeighborSearch.deepSwitch(neighbor, oldEntity, newEntity);
		}
		
		return null;
	}

	@Override
	public Map<ADemoEntity, ADemoEntity> findPairedTarget(
			IPopulation<ADemoEntity, Attribute<? extends IValue>> population, ADemoEntity predicate) {
		ADemoEntity candidateEntity = GenstarRandomUtils.oneOf(sample);
		while(candidateEntity.equals(predicate))
			candidateEntity = GenstarRandomUtils.oneOf(sample);
		return Stream.of(candidateEntity).collect(Collectors.toMap(e -> predicate, Function.identity()));
	}

	@Override
	public Collection<ADemoEntity> getPredicates() {
		return Collections.unmodifiableCollection(sample);
	}
	
	@Override
	public void setPredicates(Collection<ADemoEntity> predicates) {
		this.sample = new GosplPopulation(predicates);
	}

	@Override
	public void addPredicates(ADemoEntity predicate) {
		this.sample.add(predicate);
	}

	@Override
	public void setSample(IPopulation<ADemoEntity, Attribute<? extends IValue>> sample) {
		// TODO Auto-generated method stub
		
	}

}
