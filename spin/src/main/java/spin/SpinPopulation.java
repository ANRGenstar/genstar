package spin;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

import core.metamodel.IPopulation;
import core.metamodel.attribute.demographic.DemographicAttribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.entity.EntityUniqueId;
import core.metamodel.value.IValue;
import spin.objects.SpinNetwork;

/** Population Spin. 
 * 
 *
 */
public class SpinPopulation<E extends ADemoEntity> implements IPopulation<E, DemographicAttribute<? extends IValue>> {

	// Network associe a la population.
	private SpinNetwork network;
	

	// Interface qui permet d'avoir acces aux proprietes du reseau associe a la population.
	// (pas inclus dans le spinNetwork car fait parfois appelle a la structure graphStream pour le calcul
	// de certaines proprietes)
//	private INetProperties properties;
	
	// TODO IPopulation<APopulationEntity, APopulationAttribute, APopulationValue>
	private final Collection<E> population;

	/**
	 * 
	 * @param popRef
	 * @param prop
	 * @param network
	 */
	public SpinPopulation(IPopulation<E, DemographicAttribute<? extends IValue>> popRef, 
						 SpinNetwork network){
		population = popRef;
		this.network = network; 
	}
	
	/**
	 * Default inner type collection is {@link Set}
	 * 
	 */
	public SpinPopulation() {
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
	public SpinPopulation(Collection<E> population){
		if(!population.isEmpty())
			this.population = new HashSet<>();
		else
			this.population = population;
	}
	
	public SpinNetwork getNetwork() {
		return network;
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
	public Iterator<E> iterator() {
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
	public boolean add(E e) {
		if (population.add(e)) {
			e._setEntityId(EntityUniqueId.createNextId(this, e.getEntityType()));
			return true;
		}
		return false;
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
	public boolean addAll(Collection<? extends E> c) {
		boolean anyChange = false;
		for (E e: c) {
			anyChange = add(e) || anyChange;
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
	
	public Set<DemographicAttribute<? extends IValue>> getPopulationAttributes(){
		return population.stream().flatMap(e -> e.getAttributes().stream()).collect(Collectors.toSet());
	}
	

	@Override
	public boolean isAllPopulationOfType(String type) {
		for (ADemoEntity e: population) {
			if (type != e.getEntityType() || !type.equals(e.getEntityType()))
				return false;
		}
		return true;
	}

}
