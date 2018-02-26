package core.metamodel.attribute.emergent.filter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import core.metamodel.attribute.IAttribute;
import core.metamodel.attribute.emergent.filter.EntityChildFilterFactory.EChildFilter;
import core.metamodel.entity.IEntity;
import core.metamodel.entity.comparator.ImplicitEntityComparator;
import core.metamodel.value.IValue;

/**
 * Filter entities if they have at least one value per attribute proposed in the matches. In fact,
 * all proposed matches value are sorted according to their attribute; then every entity must match
 * with at least one value for each attribute.
 * 
 * @author kevinchapuis
 *
 */
public class EntityOneOfEachMatchFilter implements IEntityChildFilter {

	private ImplicitEntityComparator comparator;
	
	protected EntityOneOfEachMatchFilter() {
		this.comparator = new ImplicitEntityComparator();
	}
	
	protected EntityOneOfEachMatchFilter(ImplicitEntityComparator comparator) {
		this.comparator = comparator;
	}
	
	@Override
	public ImplicitEntityComparator getComparator(){
		return comparator;
	}
	
	@Override
	public Collection<IEntity<? extends IAttribute<? extends IValue>>> retain(
			Collection<IEntity<? extends IAttribute<? extends IValue>>> entities, IValue... matches) {
		Map<? extends IAttribute<? extends IValue>, Set<IValue>> eachAttribute =
				Arrays.asList(matches).stream().collect(
						Collectors.groupingBy(v -> v.getValueSpace().getAttribute(), 
								Collectors.mapping(Function.identity(), Collectors.toSet()))
						);
		return entities.stream().filter(e -> eachAttribute.keySet().stream()
				.allMatch(a -> eachAttribute.get(a).contains(e.getValueForAttribute(a.getAttributeName()))))
				.collect(Collectors.toCollection(this.getSupplier()));
	}
	
	@Override
	public void setComparator(ImplicitEntityComparator comparator) {
		this.comparator = comparator;
	}

	@Override
	public EChildFilter getType() {
		return EChildFilter.OneOfEach;
	}

}
