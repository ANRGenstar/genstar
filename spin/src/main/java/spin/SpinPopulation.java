package spin;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

import core.metamodel.IPopulation;
import core.metamodel.attribute.Attribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.entity.EntityUniqueId;
import core.metamodel.value.IValue;

/** Population Spin. 
 * 
 *
 */
public class SpinPopulation<E extends ADemoEntity> implements IPopulation<E, Attribute<? extends IValue>> {

	// Networks associated to a population
	private HashMap<String,SpinNetwork> networks;
	
	IPopulation<E, Attribute<? extends IValue>> population;

	/**
	 * 
	 * @param popRef
	 * @param prop
	 * @param network
	 */
	public SpinPopulation(IPopulation<E, Attribute<? extends IValue>> popRef){
		population = popRef;
		networks = new HashMap<>(); 
	}
	
//	/**
//	 * Default inner type collection is {@link Set}
//	 * 
//	 */
//	public SpinPopulation() {
//	//	population =  new GosplPopulation();		
//	}
	

	
	public SpinNetwork getNetwork(String networkName) {
		return networks.get(networkName);
	}
	
	public void addNetwork(String networkName, SpinNetwork network) {
		networks.put(networkName, network);
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
	
	@Override
	public SpinPopulation<E> clone() {
		return null;
	}
	
// ------------------------------------ POP ACCESSORS ------------------------------------ //
	
	public Set<Attribute<? extends IValue>> getPopulationAttributes(){
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

	@Override
	public Attribute<? extends IValue> getPopulationAttributeNamed(String name) {
		for (E e: population) {
			for (Attribute<? extends IValue> a: e.getAttributes()) {
				if (a.getAttributeName().equals(name))
					return a;
			}
		}
		return null;
	}
	
	@Override 
	public String toString() {
		String res = "Population:\n" + population + "\nNetworks: \n";
		for(SpinNetwork sp : networks.values()) {
			res = res + sp + "\n";
		}
		return res;			
	}

}
