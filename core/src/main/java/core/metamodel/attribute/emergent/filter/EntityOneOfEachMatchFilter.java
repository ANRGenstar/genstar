package core.metamodel.attribute.emergent.filter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import core.metamodel.attribute.IAttribute;
import core.metamodel.entity.IEntity;
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

	private Comparator<IEntity<? extends IAttribute<? extends IValue>>> comparator;
	
	public EntityOneOfEachMatchFilter() {
		this.comparator = this.getComparator();
	}
	
	public EntityOneOfEachMatchFilter(Comparator<IEntity<? extends IAttribute<? extends IValue>>> comparator) {
		this.comparator = comparator;
	}
	
	@Override
	public Comparator<IEntity<? extends IAttribute<? extends IValue>>> getComparator(){
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

}
