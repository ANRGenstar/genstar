package gospl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import core.metamodel.IMultitypePopulation;
import core.metamodel.IPopulation;
import core.metamodel.attribute.demographic.DemographicAttribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.value.IValue;

/**
 * The GoSPL implementation of a multitype population, that is a population able to deal with several 
 * different types of agents and browse them.
 * 
 * @author Samuel Thiriot
 *
 * @param <E>
 * @param <A>
 */
public class GosplMultitypePopulation<E extends ADemoEntity>
		implements IMultitypePopulation<E, DemographicAttribute<? extends IValue>> {

	/**
	 * Associates to each type the corresponding agents.
	 * We assume an agent is only stored for one given type.
	 */
	protected final Map<String,Set<E>> type2agents = new HashMap<>();
	
	/**
	 * Associates to each type the attributes known for this subpopulation.
	 */
	protected final Map<String,Set<DemographicAttribute<? extends IValue>>> type2attributes = new HashMap<>();
	
	private int size = 0;
	
	public GosplMultitypePopulation(String type, IPopulation<E, DemographicAttribute<? extends IValue>> pop) {
		addAll(type, pop);
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
	protected Set<DemographicAttribute<? extends IValue>> getAttributesForType(String type) {
		Set<DemographicAttribute<? extends IValue>> setForType = type2attributes.get(type);
		if (setForType == null) {
			setForType = new HashSet<>();
			type2attributes.put(type, setForType);
		}
		return setForType;
	}
	
	@Override
	public Set<DemographicAttribute<? extends IValue>> getPopulationAttributes() {
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
	
	
	/**
	 * Adds the GosplPopulation and forces all of these agents 
	 * to be of the given type. Beware this is not copying the agents, 
	 * so if you later change the original agents, the content of this collection 
	 * will be affected !
	 * @param type
	 * @param pop
	 * @return true if any change
	 */
	/*
	@SuppressWarnings("unchecked")
	public boolean addAll(String type, GosplPopulation pop) {
		
		Set<E> subpop = getSetForType(type);
		
		for (ADemoEntity e: pop) {
			e.setEntityType(type);
		}
		
		boolean anyChange = subpop.addAll((Collection<? extends E>) pop);
		
		if (anyChange)
			recomputeSize();
		
		return anyChange;
	}
	*/
	
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
		return new IteratorMultipleSets<E>(this.type2agents.values());
	}

	/**
	 * Iterator that explores successively the various Sets passed as 
	 * a parameter
	 * @author samuel Thiriot
	 *
	 * @param <E>
	 */
	private class IteratorMultipleSets<E> implements Iterator<E> {
		private final List<Set<E>> sets;
		private Iterator<Set<E>> itList;
		private Iterator<E> itCurrentSet;
		
		protected IteratorMultipleSets(Collection<Set<E>> sets) {
			this.sets = new LinkedList<Set<E>>(sets);
			this.itList = this.sets.iterator();
			try {
				this.itCurrentSet = this.itList.next().iterator();
			} catch (NullPointerException e) {
				this.itCurrentSet = null;
			}
		}

		@Override
		public boolean hasNext() {
			return itCurrentSet != null && ( itCurrentSet.hasNext() || itList.hasNext());
		}

		@Override
		public E next() {
			if (!itCurrentSet.hasNext()) {
				// we exhausted the current set
				if (!itList.hasNext())
					throw new NoSuchElementException();
				itCurrentSet = itList.next().iterator();
			}
			return itCurrentSet.next();
		}
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
		Object[] res = new Object[size];
		
		int i=0;
		for (Set<E> s : type2agents.values()) {
			for (E e: s) {
				res[i++] = e;
			}
		}
		
		return res;
	}

	@Override
	public <T> T[] toArray(T[] res) {
		
		int i=0;
		for (Set<E> s : type2agents.values()) {
			for (E e: s) {
				res[i++] = (T) e;
			}
		}
		
		return res;
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
	public IPopulation<E, DemographicAttribute<? extends IValue>> getSubPopulation(String entityType) {
		if (!type2agents.containsKey(entityType))
			throw new RuntimeException("unknown type "+entityType);
		return new GosplSubPopulation<E>(this, entityType);
	}

	@Override
	public void clear(String type) {
		getSetForType(type).clear();
		recomputeSize();
	}


	/**
	 * TODO
	 * FIXME
	 * WARNING
	 * @param parentType
	 * @return
	 */
	public Iterator<E> iterateSubPopulation(String parentType) {
		// TODO Auto-generated method stub
		return null;
	}

}
