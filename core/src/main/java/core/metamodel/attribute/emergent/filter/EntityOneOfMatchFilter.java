package core.metamodel.attribute.emergent.filter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

import core.metamodel.attribute.IAttribute;
import core.metamodel.entity.IEntity;
import core.metamodel.value.IValue;

/**
 * Filter entities if they have at least one value proposed in the matches
 * 
 * @author kevinchapuis
 *
 */
public class EntityOneOfMatchFilter implements IEntityChildFilter {

	private Comparator<IEntity<? extends IAttribute<? extends IValue>>> comparator;
	
	public EntityOneOfMatchFilter() {
		this.comparator = this.getComparator();
	}
	
	public EntityOneOfMatchFilter(Comparator<IEntity<? extends IAttribute<? extends IValue>>> comparator) {
		this.comparator = comparator;
	}
	
	@Override
	public Collection<IEntity<? extends IAttribute<? extends IValue>>> retain(
			Collection<IEntity<? extends IAttribute<? extends IValue>>> entities, IValue... matches){
		return entities.stream().filter(e -> e.getValues().stream()
				.anyMatch(v -> Arrays.asList(matches).contains(v)))
				.collect(Collectors.toCollection(this.getSupplier()));
	}
	
	@Override
	public Comparator<IEntity<? extends IAttribute<? extends IValue>>> getComparator(){
		return this.comparator;
	}

}
