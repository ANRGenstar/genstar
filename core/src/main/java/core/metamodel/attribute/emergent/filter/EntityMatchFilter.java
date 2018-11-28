package core.metamodel.attribute.emergent.filter;

import java.util.Collection;
import java.util.stream.Collectors;

import core.metamodel.attribute.IAttribute;
import core.metamodel.attribute.emergent.filter.EntityChildFilterFactory.EChildFilter;
import core.metamodel.attribute.util.AttributeVectorMatcher;
import core.metamodel.entity.IEntity;
import core.metamodel.entity.comparator.HammingEntityComparator;
import core.metamodel.entity.comparator.ImplicitEntityComparator;
import core.metamodel.value.IValue;

public class EntityMatchFilter implements IEntityChildFilter {

	private HammingEntityComparator hComparator;

	protected EntityMatchFilter() {
		this.hComparator = new HammingEntityComparator();
	}
	
	protected EntityMatchFilter(HammingEntityComparator comparator) {
		this.hComparator = comparator;
	}
	
	@Override
	public Collection<IEntity<? extends IAttribute<? extends IValue>>> retain(
			Collection<IEntity<? extends IAttribute<? extends IValue>>> entities, AttributeVectorMatcher matcher) {
		return entities.stream().filter(e -> e.getValues().stream()
				.anyMatch(v -> matcher.valueMatch(v)))
				.sorted(hComparator)
				.collect(Collectors.toCollection(this.getSupplier()));
	}

	@Override
	public ImplicitEntityComparator getComparator() {
		return null;
	}

	@Override
	public void setComparator(ImplicitEntityComparator comparator) {
		// Cannot change the comparator
	}

	@Override
	public EChildFilter getType() {
		return EChildFilter.TheOne;
	}

}
