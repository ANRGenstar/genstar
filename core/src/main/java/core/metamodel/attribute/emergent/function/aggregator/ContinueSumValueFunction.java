package core.metamodel.attribute.emergent.function.aggregator;

import java.util.Collection;

import core.metamodel.attribute.IValueSpace;
import core.metamodel.value.numeric.ContinuousValue;

/**
 * Sums up the continuous value as the aggregated process
 * 
 * @author kevinchapuis
 *
 */
public class ContinueSumValueFunction implements IAggregateValueFunction<ContinuousValue, ContinuousValue> {

	public IValueSpace<ContinuousValue> cs;
	
	public ContinueSumValueFunction(IValueSpace<ContinuousValue> cs) {
		this.cs = cs;
	}
	
	@Override
	public ContinuousValue aggregate(Collection<ContinuousValue> values) {
		return cs.proposeValue(Double.toString(values.stream()
				.mapToDouble(v -> v.getActualValue()).sum()));
	}

	@Override
	public IValueSpace<ContinuousValue> getValueSpace() {
		return this.cs;
	}

}
