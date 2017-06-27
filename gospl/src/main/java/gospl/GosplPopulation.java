package gospl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

import core.metamodel.IPopulation;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;

public class GosplPopulation implements IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> {
	
	private final Collection<APopulationEntity> population;
	private Collection<APopulationAttribute> attributes = null;
	
	/**
	 * Default inner type collection is {@link Set}
	 * 
	 */
	public GosplPopulation() {
		population = new HashSet<>();
	}
	
	/**
	 * Place the concrete type of collection you want this population be. If the propose
	 * collection is not empty, then default inner collection type is choose.
	 * 
	 * @see GosplPopulation()
	 * 
	 * @param population
	 */
	public GosplPopulation(Collection<APopulationEntity> population){
		if(!population.isEmpty())
			this.population = new HashSet<>();
		else
			this.population = population;
	}
	
	/**
	 * throws an exception if this entity does not has the reference attributes
	 * @param e
	 */
	protected final void _checkEntityAttributes(APopulationEntity e) throws IllegalArgumentException {
		if ((this.attributes != null) && (!e.getAttributes().equals(this.attributes)))
			throw new IllegalArgumentException(
					"the entity should contain attributes "+
					attributes.toString()
			);
			
	}
	
	/** 
	 * defines the attributes expected to be present for all the entities
	 * in this population. Further calls to add() will raise exceptions if these attributes
	 * are not defined for the novel entity.
	 * 
	 * @param attributes
	 */
	public void setExpectedAttributes(Collection<APopulationAttribute> attributes) {
		this.attributes = attributes;
		
		// check past entities
		for (APopulationEntity e: population) {
			_checkEntityAttributes(e);
		}
	}

	@Override
	public int size() {
		return population.size();
	}

	@Override
	public boolean isEmpty() {
		return population.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return population.contains(o);
	}

	@Override
	public Iterator<APopulationEntity> iterator() {
		return population.iterator();
	}

	@Override
	public Object[] toArray() {
		return population.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return population.toArray(a);
	}

	@Override
	public boolean add(APopulationEntity e) {
		if (attributes != null)
			_checkEntityAttributes(e);
		return population.add(e);
	}

	@Override
	public boolean remove(Object o) {
		return population.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return population.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends APopulationEntity> c) {
		return population.addAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return population.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return population.retainAll(c);
	}

	@Override
	public void clear() {
		population.clear();
	}
	
// ------------------------------------ POP ACCESSORS ------------------------------------ //
	
	public Set<APopulationAttribute> getPopulationAttributes(){
		if (attributes == null)
			// rebuild the list of attributes
			return population.parallelStream().flatMap(e -> e.getAttributes().stream()).collect(Collectors.toSet());
		else 
			return new HashSet<>(attributes);
	}
	
}
