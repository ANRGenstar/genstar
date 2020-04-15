package gospl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.Streams;

import core.metamodel.IMultitypePopulation;
import core.metamodel.IPopulation;
import core.metamodel.attribute.Attribute;
import core.metamodel.attribute.IAttribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.entity.EntityUniqueId;
import core.metamodel.entity.IEntity;
import core.metamodel.value.IValue;

/**
 * The GoSPL implementation of a multitype population, that is a population able to deal with several 
 * different types of agents and browse them.
 * 
 * TODO : may be move to a population of GosplEntity
 * 
 * @author Samuel Thiriot
 *
 * @param <E>
 * @param <A>
 */
public class GosplMultitypePopulation<E extends ADemoEntity>
		implements IMultitypePopulation<E, Attribute<? extends IValue>> {
	
	public static final String LAYER_ID = "Layer_";
	
	/**
	 * Keep the hierarchical relationship between types of entities. As a default
	 * behavior level will be associated with insertion order
	 */
	protected final Map<String, Integer> typeToLevel = new HashMap<>();
	
	/**
	 * Associates to each type the corresponding agents.
	 * We assume an agent is only stored for one given type.
	 */
	protected final Map<String,Set<E>> type2agents = new HashMap<>();
	
	/**
	 * Associates to each type the attributes known for this subpopulation.
	 */
	protected final Map<String,Set<Attribute<? extends IValue>>> type2attributes = new HashMap<>();
	
	// TODO : remove this attribute
	private int size = 0;
	
	/**
	 * Build a population with given type of entities from a population
	 * @param type
	 * @param pop
	 */
	public GosplMultitypePopulation(String type, IPopulation<E, Attribute<? extends IValue>> pop) {
		addAll(type, pop);
	}
	
	/**
	 * Entity type order matters for level
	 * 
	 * @param entityTypes
	 */
	public GosplMultitypePopulation(String... entityTypes) {
		int index = 0;
		for (String type: entityTypes) {
			type2agents.put(type, new HashSet<>());
			typeToLevel.put(type, index++);
			type2attributes.put(type, new HashSet<>());
		}
	}
	
	/**
	 * Entity type order matters for level
	 * 
	 * @param entityTypes
	 */
	public GosplMultitypePopulation(List<String> entityTypes) {
		for (String type : entityTypes) {
			type2agents.put(type, new HashSet<>());
			typeToLevel.put(type, entityTypes.indexOf(type));
			type2attributes.put(type, new HashSet<>());
		}
	}

	// Specific constructor/builder ------------ //
	
	@SuppressWarnings("unchecked")
	public static <E extends ADemoEntity> GosplMultitypePopulation<E> getMultiPopulation(Collection<E> entities, boolean cloneEntities) {
		List<Collection<E>> subLayerEntities = new ArrayList<>();
		List<String> layerID = new ArrayList<>();
		boolean hasChild = entities.stream().anyMatch(e -> e.hasChildren());
		if(hasChild) {
			Collection<E> slEntities = entities;
			while(hasChild) {
				slEntities =  slEntities.stream()
						.flatMap(e -> e.getChildren().stream()).map(e -> (E)e)
						.collect(Collectors.toSet());
				subLayerEntities.add(slEntities);
				hasChild = slEntities.stream().anyMatch(e -> e.hasChildren());
			}
		}
		List<Collection<E>> supLayerEntities = new ArrayList<>();
		boolean hasMother = entities.stream().anyMatch(e -> e.hasParent());
		if(hasMother) {
			Collection<E> slEntities = entities;
			while(hasMother) {
				slEntities =  slEntities.stream()
						.map(e -> (E) e.getParent())
						.collect(Collectors.toSet());
				subLayerEntities.add(slEntities);
				hasMother = slEntities.stream().anyMatch(e -> e.hasParent());
			}
		}
		
		// Pop layered collected
		List<Collection<E>> pop = Streams.concat(
					subLayerEntities.stream(),
					Stream.of(entities),
					supLayerEntities.stream())
				.collect(Collectors.toList());
		
		for(Collection<E> sPop : pop) {
			Set<String> layerTypes= sPop.stream().map(E::getEntityType).collect(Collectors.toSet());
			if(layerTypes.isEmpty() || layerTypes.size()!=1) { layerID.add( LAYER_ID+pop.indexOf(sPop) ); }
			else { layerID.add(layerTypes.iterator().next()); }
		}
		
		// Build pop and dispatch entities
		GosplMultitypePopulation<E> gmp = new GosplMultitypePopulation<>(layerID);
		for(int i = 0; i < layerID.size(); i++){ 
			gmp.addAll(layerID.get(i), pop.get(i).stream()
					.map(e -> (E) (cloneEntities ? e.clone() : e))
					.collect(Collectors.toSet())); 
		}
		
		return gmp;
	}
	
	/**
	 * Protected constructor to clone multi population
	 * @param levelID
	 * @param subPops
	 */
	protected GosplMultitypePopulation(List<String> levelID, Map<String, IPopulation<E, Attribute<? extends IValue>>> subPops) {
		for(String id : levelID) { addAll(id, subPops.get(id)); }
	}
	
	// ----------------------------------------- //
	
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
			// update levels
			if(typeToLevel.isEmpty()) {typeToLevel.put(type, 0);}
			else { typeToLevel.put(type, Collections.max(typeToLevel.values())+1); }
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
	protected Set<Attribute<? extends IValue>> getAttributesForType(String type) {
		Set<Attribute<? extends IValue>> setForType = type2attributes.get(type);
		if (setForType == null) {
			setForType = new HashSet<>();
			type2attributes.put(type, setForType);
		}
		return setForType;
	}
	
	@Override
	public Set<Attribute<? extends IValue>> getPopulationAttributes() {
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
			e._setEntityId(EntityUniqueId.createNextId(this, e.getEntityType()));
			this.size++;
			for(IEntity<? extends IAttribute<? extends IValue>> child : e.getChildren()) {
				@SuppressWarnings("unchecked") E c = (E)child; this.add(c); // Possible infinite recurrent call
			}
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public boolean add(String type, E e) {
		
		System.err.println("addedinf it multitype pop entity with forced type "+type);

		e.setEntityType(type);
		if (getSetForType(type).add(e)) {
			this.size++;
			e._setEntityId(EntityUniqueId.createNextId(this, type));
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
	
	/**
	 * Adds the GosplPopulation and forces all of these agents 
	 * to be of the given type. Beware this is not copying the agents, 
	 * so if you later change the original agents, the content of this collection 
	 * will be affected !
	 * 
	 * <b>note that you have to clone the agents yourself before inserting them!</b>
	 * @param type
	 * @param pop
	 * @return true if any change
	 */
	@Override
	public boolean addAll(String type, Collection<? extends E> c) {
		
		// ensure the type of these agents is the right one
		for (E e: c) {
			e.setEntityType(type);
		}
		
		// add the agents in the corresponding subset devoded to this type.
		boolean anyChange = false;
		final Set<E> setForType = getSetForType(type);
		for (E e: c) {
			if (setForType.add(e)) {
				// the entity was not there already
				anyChange = true;
				e._setEntityId(EntityUniqueId.createNextId(this, type));
				this.size++;
			}
		}
		
		// adds the corresponding attributes
		Set<Attribute<? extends IValue>> s = type2attributes.get(type);
		if (s == null) {
			s = new HashSet<>();
			type2attributes.put(type, s);
		}
		s.addAll(c.stream().flatMap(e -> e.getAttributes().stream())
				.collect(Collectors.toSet()));
		
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
		return new IteratorMultipleSets<E>(this.type2agents.values());
	}

	/**
	 * Iterator that explores successively the various Sets passed as 
	 * a parameter
	 * @author samuel Thiriot
	 *
	 * @param <ET>
	 */
	private class IteratorMultipleSets<ET> implements Iterator<ET> {
		private final List<Set<ET>> sets;
		private Iterator<Set<ET>> itList;
		private Iterator<ET> itCurrentSet;
		
		protected IteratorMultipleSets(Collection<Set<ET>> sets) {
			this.sets = new LinkedList<Set<ET>>(sets);
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
		public ET next() {
			if (!itCurrentSet.hasNext()) {
				// we exhausted the current set
				if (!itList.hasNext())
					throw new NoSuchElementException();
				itCurrentSet = itList.next().iterator();
			}
			return itCurrentSet.next();
		}
	}
	
	/**
	 * Will also remove children of this E entity, with nasty casts
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(Object o) {
		for (Set<E> s: type2agents.values()) {
			if (s.remove(o)) {
				this.size--;
				Collection<E> ec = Stream.of((E)o).collect(Collectors.toSet()); 
				boolean removeChild = false;
				while(ec.stream().anyMatch(e -> e.hasChildren())) {
					removeChild = false;
					ec = ec.stream().flatMap(e -> e.getChildren().stream())
							.map(c -> (E)c).collect(Collectors.toSet());
					for(Set<E> subs : type2agents.values()) {
						if(subs.removeAll(ec)) { removeChild = true; }
					}
				}
				return removeChild;
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

	@SuppressWarnings("unchecked")
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
	public int getEntityLevel(String type) {
		return this.typeToLevel.get(type);
	}


	@Override
	public List<Integer> getEntityLevel() {
		return new ArrayList<>(this.typeToLevel.values());
	}


	@Override
	public String getEntityType(int level) {
		return this.typeToLevel.keySet().stream()
				.filter(k -> typeToLevel.get(k)==level)
				.findFirst().orElseThrow(NullPointerException::new);
	}

	@Override
	public void addEntityType(String novelType) {
		if (!type2agents.containsKey(novelType))
			type2agents.put(novelType, new HashSet<>());
	}

	@Override
	public IPopulation<E, Attribute<? extends IValue>> getSubPopulation(String entityType) {
		if (!type2agents.containsKey(entityType))
			throw new RuntimeException("unknown type "+entityType);
		return new GosplSubPopulation<E>(this, entityType);
	}
	

	@Override
	public IPopulation<E, Attribute<? extends IValue>> getSubPopulation(int entityLevel) {
		return this.getSubPopulation(this.getEntityType(entityLevel));
	}
	
	/**
	 * Get the size of the layered (level from 0 -- i.e. no child   -- to n -- i.e. no parent) sub population
	 * @param layer
	 * @return
	 */
	public int getSubPopulationSize(int layer) {
		return this.getSetForType(this.getEntityType(layer)).size();
	}

	@Override
	public void clear(String type) {
		getSetForType(type).clear();
		recomputeSize();
	}
	
	@Override
	public GosplMultitypePopulation<E> clone() {
		GosplMultitypePopulation<E> gmp = new GosplMultitypePopulation<>(typeToLevel.values().stream().sorted().map(
				level -> this.getEntityType(level)).collect(Collectors.toList()));
		gmp.type2attributes.putAll(this.type2attributes);
		gmp.type2agents.putAll(this.type2agents);
		gmp.recomputeSize();
		return gmp;
	}

	@Override
	public Iterator<E> iterateSubPopulation(String parentType) {
		try {
			return type2agents.get(parentType).iterator();
		} catch (NullPointerException e) {
			throw new NullPointerException("no entity of type "+parentType
					+" in this "+this.getClass().getSimpleName());
		}
	}

	@Override
	public boolean isAllPopulationOfType(String type) {
		return type2agents.size() == 1 && type2agents.get(type) != null;
	}


	@Override
	public Attribute<? extends IValue> getPopulationAttributeNamed(String name) {
		for (Set<Attribute<? extends IValue>> attributes: type2attributes.values()) {
			for (Attribute<? extends IValue> a: attributes) {
				if (a.getAttributeName().equals(name))
					return a;
			}
		}
		return null;
	}
	
}
