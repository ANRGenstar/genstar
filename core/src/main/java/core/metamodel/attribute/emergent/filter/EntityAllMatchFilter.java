package core.metamodel.attribute.emergent.filter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

import core.metamodel.attribute.IAttribute;
import core.metamodel.entity.IEntity;
import core.metamodel.value.IValue;

public class EntityAllMatchFilter<E extends IEntity<? extends IAttribute<? extends IValue>>> 
	implements IEntityChildFilter<E> {
	
	Comparator<E> comparator;
	
	public EntityAllMatchFilter() {
		this.comparator = this.getComparator();
	}
	
	public EntityAllMatchFilter(Comparator<E> comparator) {
		this.comparator = comparator;
	}
	
	@Override
	public Collection<E> retain(Collection<E> entities, IValue... matches){
		return entities.stream().filter(e -> e.getValues().containsAll(Arrays.asList(matches)))
				.collect(Collectors.toCollection(this.getSupplier()));
	}
	
	@Override
	public Comparator<E> getComparator(){
		return comparator;
	}
	
}
