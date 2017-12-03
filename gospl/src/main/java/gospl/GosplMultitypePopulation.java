package gospl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import core.metamodel.IMultitypePopulation;
import core.metamodel.IPopulation;
import core.metamodel.attribute.IAttribute;
import core.metamodel.attribute.demographic.DemographicAttribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.entity.IEntity;
import core.metamodel.value.IValue;


public class GosplMultitypePopulation<E extends IEntity<A>, A extends IAttribute<? extends IValue>>
		implements IMultitypePopulation<E, A> {

	protected final Map<String,Set<E>> type2agents = new HashMap<>();
	
	protected final Map<String,Set<A>> type2attributes = new HashMap<>();
	
	private int size = 0;
	
	public GosplMultitypePopulation() {
		
	}
	
	public GosplMultitypePopulation(String... entityTypes) {
		for (String type: entityTypes) {
			type2agents.put(type, new HashSet<>());
			type2attributes.put(type, new HashSet<>());
		}
	}
	
	protected void recomputeSize() {
		size = 0;
		for (Set<E> s: type2agents.values()) {
			size += s.size();
		} 
	}
	
	/**
	 * Returns the internal set storing this type of agent.
	 * Creates it on the fly if required
	 * @param type
	 * @return
	 */
	protected Set<E> getSetForType(String type) {
		Set<E> setForType = type2agents.get(type);
		if (setForType == null) {
			setForType = new HashSet<>();
			type2agents.put(type, setForType);
		}
		return setForType;
	}
	
	
	/**
	 * Returns the internal set storing attributes 
	 * for this type of agent
	 * @param type
	 * @return
	 */
	protected Set<A> getAttributesForType(String type) {
		Set<A> setForType = type2attributes.get(type);
		if (setForType == null) {
			setForType = new HashSet<>();
			type2attributes.put(type, setForType);
		}
		return setForType;
	}
	
	@Override
	public Set<A> getPopulationAttributes() {
		return type2attributes.values().stream()
					.flatMap(coll -> coll.stream())
					.collect(Collectors.toSet());
	}

	@Override
	public boolean add(E e) {
		
		if (!e.hasEntityType())
			throw new RuntimeException("the population entity should be given an entity type");
				 
		if (getSetForType(e.getEntityType()).add(e)) {
			type2attributes.get(e.getEntityType()).addAll(e.getAttributes());
			this.size++;
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public boolean add(String type, E e) {
		
		e.setEntityType(type);
		if (getSetForType(e.getEntityType()).add(e)) {
			this.size++;
			return true;
		} else {
			return false;
		}
		 
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		
		boolean anychange = false;
		for (E e : c)
			anychange = this.add(e) || anychange;
		
		if (anychange)
			recomputeSize();
		
		return anychange;
	}
	
	@Override
	public boolean addAll(String type, Collection<? extends E> c) {
		for (E e: c) {
			e.setEntityType(type);
		}
		boolean anyChange = getSetForType(type).addAll(c);
		
		if (anyChange)
			recomputeSize();
		
		return anyChange;
	}


	@Override
	public void clear() {
		for (Set<E> s: type2agents.values())
			s.clear();
		this.size = 0;
	}

	@Override
	public boolean contains(Object o) {
		for (Set<E> s: type2agents.values()) {
			if (s.contains(o))
				return true;
		}
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		
		Set<?> notFound = new HashSet<>(c);
		
		for (Set<E> s: type2agents.values()) {
			notFound.removeAll(s);
		}
		
		return notFound.isEmpty();
	}

	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	@Override
	public Iterator<E> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean remove(Object o) {
		for (Set<E> s: type2agents.values()) {
			if (s.remove(o)) {
				this.size--;
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean anyChange = false;
		for (Set<E> s: type2agents.values()) {
			anyChange = s.removeAll(c) || anyChange;
		}
		if (anyChange)
			recomputeSize();
		
		return anyChange;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		boolean anyChange = false;
		for (Set<E> s: type2agents.values()) {
			anyChange = s.retainAll(c) || anyChange;
		}
		if (anyChange)
			recomputeSize();
		return anyChange;
	}

	@Override
	public int size() {
		return this.size;
	}

	@Override
	public Object[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T[] toArray(T[] a) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getEntityTypes() {
		return type2agents.keySet();
	}

	@Override
	public void addEntityType(String novelType) {
		if (!type2agents.containsKey(novelType))
			type2agents.put(novelType, new HashSet<>());
	}

	@Override
	public IPopulation<E, A> getSubPopulation(String entityType) {
		if (!type2agents.containsKey(entityType))
			throw new RuntimeException("unknown type "+entityType);
		return new GosplSubPopulation<E,A>(this, entityType);
	}

	@Override
	public void clear(String type) {
		getSetForType(type).clear();
		recomputeSize();
	}

	

}
