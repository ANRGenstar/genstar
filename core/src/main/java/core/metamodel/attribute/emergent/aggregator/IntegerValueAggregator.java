package core.metamodel.attribute.emergent.aggregator;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonTypeName;

import core.metamodel.value.IValueSpace;
import core.metamodel.value.numeric.IntegerValue;

@JsonTypeName(IntegerValueAggregator.SELF)
public class IntegerValueAggregator implements IAggregatorValueFunction<IntegerValue> {

	public static final String SELF = IAggregatorValueFunction.DEFAULT_TAG+"INT AGGREGATOR";
	private static final IntegerValueAggregator INSTANCE = new IntegerValueAggregator();
	
	private IntegerValueAggregator() {}
	
	public static IntegerValueAggregator getInstance() {
		return INSTANCE;
	}
	
	@Override
	public IntegerValue transpose(Collection<IntegerValue> values, IValueSpace<IntegerValue> vs) {
		return vs.proposeValue(Integer.toString(values.stream()
				.mapToInt(v -> v.getActualValue()).sum()));
	}

	@Override
	public String getType() {
		return SELF;
	}
	
}
