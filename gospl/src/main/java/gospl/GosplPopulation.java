package gospl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

import core.metamodel.IPopulation;
import core.metamodel.attribute.Attribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.entity.EntityUniqueId;
import core.metamodel.value.IValue;
import gospl.io.util.ReadPopulationsUtils;

/**
 * To create a population from a sample read from file, refer to {@link ReadPopulationsUtils}
 * 
 * @author Samuel Thiriot
 *
 */
public class GosplPopulation implements IPopulation<ADemoEntity, Attribute<? extends IValue>> {
	
	
	private final Collection<ADemoEntity> population;
	private Set<Attribute<? extends IValue>> attributes = null;
	
	/**
	 * Default inner type collection is {@link Set}
	 * 
	 */
	public GosplPopulation() {
		population = new HashSet<>();
	}
	
	/**
	 * "Clone" operator 
	 * @param populationOfParentCandidates
	 */
	public GosplPopulation(GosplPopulation pop) {
		
		this.population = new HashSet<>();
		for (ADemoEntity original : pop.population) {
			ADemoEntity clone = original.clone();
			this.population.add(clone);
		}

		this.attributes = new HashSet<>(pop.getPopulationAttributes());
	}
	
	
	/**
	 * Population with a given collection of entity within
	 * 
	 * @see GosplPopulation()
	 * 
	 * @param population
	 */
	public GosplPopulation(Collection<ADemoEntity> population){
		if(population.isEmpty())
			this.population = new HashSet<>();
		else
			this.population = new HashSet<>(population);
	}
	
	/**
	 * throws an exception if this entity does not has the reference attributes
	 * @param e
	 */
	protected final void _checkEntityAttributes(ADemoEntity e) throws IllegalArgumentException {
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
	public void setExpectedAttributes(Collection<Attribute<IValue>> attributes) {
		this.attributes = new HashSet<>(attributes);
		
		// check past entities
		for (ADemoEntity e: population) {
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
	public Iterator<ADemoEntity> iterator() {
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
	public boolean add(ADemoEntity e) {
		if (attributes != null)
			_checkEntityAttributes(e);
		if (population.add(e)) {
			// the agent was just added into the population
			e._setEntityId(EntityUniqueId.createNextId(this, e.getEntityType()));
			return true;
		} else {
			return false;
		}
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
	public boolean addAll(Collection<? extends ADemoEntity> c) {
		boolean anyChange = false;
		for (ADemoEntity e: c) {
			anyChange = this.add(e) || anyChange;
		}
		return anyChange;
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
	
	public Set<Attribute<? extends IValue>> getPopulationAttributes(){
		if (attributes == null) {
			// rebuild the list of attributes
			attributes = population.stream().flatMap(e -> e.getAttributes().stream()).collect(Collectors.toSet());
			return Collections.unmodifiableSet(attributes);
		} else {
			return Collections.unmodifiableSet(attributes);
		}
	}

	@Override
	public boolean isAllPopulationOfType(String type) {
		for (ADemoEntity e: population) {
			if (type != e.getEntityType() || !type.equals(e.getEntityType()))
				return false;
		}
		return true;
	}

	@Override
	public Attribute<? extends IValue> getPopulationAttributeNamed(String name) {
		attributes = getPopulationAttributes();
		for (Attribute<? extends IValue> a: attributes) {
			if (a.getAttributeName().equals(name))
				return a;
		}
		return null;
	}
	
}
