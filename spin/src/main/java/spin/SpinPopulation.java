package spin;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

import core.metamodel.IPopulation;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;
import spin.interfaces.INetProperties;
import spin.objects.SpinNetwork;
import useless.StatFactory;

/** Population Spin. 
 * 
 *
 */
public class SpinPopulation implements IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> {

	// Network associé a la population.
	private SpinNetwork network;
	// TODO [stage] a remplacer par un graphstream
	

	// Interface qui permet d'avoir acces aux propriétés du réseau associé a la population.
	// (pas inclus dans le spinNetwork car fait parfois appelle a la structure graphStream pour le calcul
	// de certaines propriétés)
//	private INetProperties properties;
	
	// TODO IPopulation<APopulationEntity, APopulationAttribute, APopulationValue>
	private final Collection<APopulationEntity> population;

	/**
	 * 
	 * @param popRef
	 * @param prop
	 * @param network
	 */
	public SpinPopulation(IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> popRef, 
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
	public SpinPopulation(Collection<APopulationEntity> population){
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
		return population.parallelStream().flatMap(e -> e.getAttributes().stream()).collect(Collectors.toSet());
	}

}
