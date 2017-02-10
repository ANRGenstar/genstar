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
import spin.algo.factory.StatFactory;
import spin.interfaces.ISpinNetProperties;

/** Population Spin. 
 * 
 *
 */
public class SpinPopulation implements IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> {

	// Interface qui permet d'avoir acces aux propriétés du réseau associé a la population. 
	private ISpinNetProperties properties;
	
	/**
	 * Permet d'assurer d'instancier l'interface avant la 1er utilisation
	 * mais évite cette instanciation dans tous les cas d'utilisation.
	 * ( Conversion du SpinNetwork en graphStream )
	 * @return
	 */
	public ISpinNetProperties getProperties() {
		if(properties == null)
			properties = StatFactory.getInstance();
		return properties;
	}

	private final Collection<APopulationEntity> population;
	
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
