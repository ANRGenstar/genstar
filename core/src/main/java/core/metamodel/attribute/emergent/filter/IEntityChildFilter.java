package core.metamodel.attribute.emergent.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.function.Supplier;

import core.metamodel.attribute.IAttribute;
import core.metamodel.entity.IEntity;
import core.metamodel.value.IValue;

/**
 * Filter a set of entity according to value matching
 * 
 * @author kevinchapuis
 *
 * @param <E>
 */
public interface IEntityChildFilter<E extends IEntity<? extends IAttribute<? extends IValue>>> {
	
	/**
	 * Retain only entities that match values - with custom matching rule, e.g. all or one of match
	 * 
	 * @param entities
	 * @param filters
	 * @return
	 */
	public Collection<E> retain(Collection<E> entities, IValue... matches); 
	
	/**
	 * Chose only one entity among a collect of entities. Have to be consistent, that is the same
	 * arguments must always return the same entity
	 * 
	 * @param entities
	 * @param matches
	 * @return
	 */
	default E choseOne(Collection<E> entities, IValue... matches) {
		List<E> retains = new ArrayList<>(this.retain(entities, matches));
		Collections.sort(retains, this.getComparator());
		return retains.get(0);
	}
	
	/**
	 * The default supplier to collect retained entities
	 * @return
	 */
	default Supplier<Collection<E>> getSupplier(){
		return new Supplier<Collection<E>>() {
			@Override
			public Collection<E> get() {
				return new HashSet<E>();
			}
		};
	}
	
	/**
	 * The default comparator of entities that compare entity ID
	 * @return
	 */
	default Comparator<E> getComparator(){
		return new Comparator<E>() {
			@Override
			public int compare(E o1, E o2) {
				return o1.getEntityId().compareTo(o2.getEntityId());
			}
		};
	}
	
}
