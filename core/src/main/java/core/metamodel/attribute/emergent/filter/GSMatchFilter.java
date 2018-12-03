package core.metamodel.attribute.emergent.filter;

import java.util.Collection;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonTypeName;

import core.metamodel.attribute.IAttribute;
import core.metamodel.entity.IEntity;
import core.metamodel.entity.matcher.IGSEntityMatcher;
import core.metamodel.entity.matcher.MatchType;
import core.metamodel.value.IValue;

/**
 * Filter entity by matching at least one attribute with vector matcher.
 *  
 * @author kevinchapuis
 *
 */
@JsonTypeName(GSMatchFilter.SELF)
public class GSMatchFilter<T> extends  AGSEntityTransposer<Collection<IEntity<? extends IAttribute<? extends IValue>>>, T> {
		
	public static final String SELF = "MATCH FILTER";
	
	public GSMatchFilter(IGSEntityMatcher<T> matcher, MatchType match) {
		super(matcher, match);
	}

	@Override
	public Collection<IEntity<? extends IAttribute<? extends IValue>>> apply(IEntity<? extends IAttribute<? extends IValue>> superEntity) {
		return superEntity.getChildren().stream()
				.filter(e -> this.validate(super.getMatchType(), e))
				.sorted(super.getComparator())
				.collect(Collectors.toCollection(this.getSupplier()));
	}
	
}
