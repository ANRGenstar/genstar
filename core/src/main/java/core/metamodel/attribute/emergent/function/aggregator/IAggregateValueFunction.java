package core.metamodel.attribute.emergent.function.aggregator;

import java.util.Collection;

import core.metamodel.attribute.IValueSpace;
import core.metamodel.value.IValue;

public interface IAggregateValueFunction<RV extends IValue, IV extends IValue> {

	public RV aggregate(Collection<IV> values);

	public IValueSpace<RV> getValueSpace();
	
}
