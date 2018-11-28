package core.metamodel.attribute.emergent.filter;

import java.util.Collection;
import java.util.stream.Collectors;

import core.metamodel.attribute.IAttribute;
import core.metamodel.attribute.emergent.filter.EntityChildFilterFactory.EChildFilter;
import core.metamodel.attribute.util.AttributeVectorMatcher;
import core.metamodel.entity.IEntity;
import core.metamodel.entity.comparator.ImplicitEntityComparator;
import core.metamodel.value.IValue;

/**
 * Filter entities if they have at least one value proposed in the matches
 * 
 * @author kevinchapuis
 *
 */
public class EntityOneOfMatchFilter implements IEntityChildFilter {

	private ImplicitEntityComparator comparator;
	
	protected EntityOneOfMatchFilter() {
		this.comparator = new ImplicitEntityComparator();
	}
	
	protected EntityOneOfMatchFilter(ImplicitEntityComparator comparator) {
		this.comparator = comparator;
	}
	
	@Override
	public Collection<IEntity<? extends IAttribute<? extends IValue>>> retain(
			Collection<IEntity<? extends IAttribute<? extends IValue>>> entities, AttributeVectorMatcher matcher){
		return entities.stream().filter(e -> e.getValues().stream()
				.anyMatch(v -> matcher.valueMatch(v)))
				.collect(Collectors.toCollection(this.getSupplier()));
	}
	
	@Override
	public ImplicitEntityComparator getComparator(){
		return this.comparator;
	}

	@Override
	public void setComparator(ImplicitEntityComparator comparator) {
		this.comparator = comparator;
	}

	@Override
	public EChildFilter getType() {
		return EChildFilter.OneOf;
	}

}
