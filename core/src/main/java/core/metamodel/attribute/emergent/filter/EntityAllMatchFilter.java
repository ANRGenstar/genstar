package core.metamodel.attribute.emergent.filter;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import core.metamodel.attribute.IAttribute;
import core.metamodel.attribute.emergent.filter.EntityChildFilterFactory.EChildFilter;
import core.metamodel.entity.IEntity;
import core.metamodel.entity.comparator.ImplicitEntityComparator;
import core.metamodel.value.IValue;

/**
 * Filter entities if they match with all value given as matches
 * 
 * @author kevinchapuis
 *
 */
public class EntityAllMatchFilter implements IEntityChildFilter {
	
	private ImplicitEntityComparator comparator;
	
	protected EntityAllMatchFilter() {
		this.comparator = new ImplicitEntityComparator();
	}
	
	protected EntityAllMatchFilter(ImplicitEntityComparator comparator) {
		this.comparator = comparator;
	}
	
	@Override
	public ImplicitEntityComparator getComparator(){
		return comparator;
	}
	
	@Override
	public Collection<IEntity<? extends IAttribute<? extends IValue>>> retain(
			Collection<IEntity<? extends IAttribute<? extends IValue>>> entities, IValue... matches){
		return entities.stream().filter(e -> e.getValues().containsAll(Arrays.asList(matches)))
				.collect(Collectors.toCollection(this.getSupplier()));
	}
	
	@Override
	public void setComparator(ImplicitEntityComparator comparator) {
		this.comparator = comparator;
	}

	@Override
	public EChildFilter getType() {
		return EChildFilter.All;
	}
	
}
