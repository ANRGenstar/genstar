package spin;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

import core.metamodel.IPopulation;
import core.metamodel.pop.ADemoEntity;
import core.metamodel.pop.attribute.DemographicAttribute;
import core.metamodel.value.IValue;
import spin.objects.SpinNetwork;

/** Population Spin. 
 * 
 *
 */
public class SpinPopulation implements IPopulation<ADemoEntity, DemographicAttribute<? extends IValue>> {

	// Network associe a la population.
	private SpinNetwork network;
	

	// Interface qui permet d'avoir acces aux proprietes du reseau associe a la population.
	// (pas inclus dans le spinNetwork car fait parfois appelle a la structure graphStream pour le calcul
	// de certaines proprietes)
//	private INetProperties properties;
	
	// TODO IPopulation<APopulationEntity, APopulationAttribute, APopulationValue>
	private final Collection<ADemoEntity> population;

	/**
	 * 
	 * @param popRef
	 * @param prop
	 * @param network
	 */
	public SpinPopulation(IPopulation<ADemoEntity, DemographicAttribute<? extends IValue>> popRef, 
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
	public SpinPopulation(Collection<ADemoEntity> population){
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
	public boolean addAll(Collection<? extends ADemoEntity> c) {
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
	
	public Set<DemographicAttribute<? extends IValue>> getPopulationAttributes(){
		return population.stream().flatMap(e -> e.getAttributes().stream()).collect(Collectors.toSet());
	}

}
