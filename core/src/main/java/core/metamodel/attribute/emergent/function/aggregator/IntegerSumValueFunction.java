package core.metamodel.attribute.emergent.function.aggregator;

import java.util.Collection;

import core.metamodel.attribute.IValueSpace;
import core.metamodel.value.numeric.IntegerValue;

/**
 * Sum up the integer values as the aggregated process
 * 
 * @author kevinchapuis
 *
 */
public class IntegerSumValueFunction implements IAggregateValueFunction<IntegerValue, IntegerValue> {

	private IValueSpace<IntegerValue> is;
	
	public IntegerSumValueFunction(IValueSpace<IntegerValue> is) {
		this.is = is;
	}
	
	@Override
	public IntegerValue aggregate(Collection<IntegerValue> values) {
		return is.proposeValue(Integer.toString(values.stream()
				.mapToInt(v -> v.getActualValue()).sum()));
	}

	@Override
	public IValueSpace<IntegerValue> getValueSpace() {
		return this.is;
	}

}
