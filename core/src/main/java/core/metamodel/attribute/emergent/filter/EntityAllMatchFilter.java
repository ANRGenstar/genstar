package core.metamodel.attribute.emergent.filter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

import core.metamodel.attribute.IAttribute;
import core.metamodel.attribute.emergent.filter.EntityChildFilterFactory.EChildFilter;
import core.metamodel.entity.IEntity;
import core.metamodel.value.IValue;

/**
 * Filter entities if they match with all value given as matches
 * 
 * @author kevinchapuis
 *
 */
public class EntityAllMatchFilter implements IEntityChildFilter {
	
	private Comparator<IEntity<? extends IAttribute<? extends IValue>>> comparator;
	
	public EntityAllMatchFilter() {
		this.comparator = this.getDefaultComparator();
	}
	
	public EntityAllMatchFilter(Comparator<IEntity<? extends IAttribute<? extends IValue>>> comparator) {
		this.comparator = comparator;
	}
	
	@Override
	public Comparator<IEntity<? extends IAttribute<? extends IValue>>> getComparator(){
		return comparator;
	}
	
	@Override
	public Collection<IEntity<? extends IAttribute<? extends IValue>>> retain(
			Collection<IEntity<? extends IAttribute<? extends IValue>>> entities, IValue... matches){
		return entities.stream().filter(e -> e.getValues().containsAll(Arrays.asList(matches)))
				.collect(Collectors.toCollection(this.getSupplier()));
	}
	
	@Override
	public void setComparator(Comparator<IEntity<? extends IAttribute<? extends IValue>>> comparator) {
		this.comparator = comparator;
	}

	@Override
	public EChildFilter getType() {
		return EChildFilter.All;
	}
	
}
