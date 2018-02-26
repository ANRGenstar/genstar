package core.metamodel.attribute.emergent.aggregator;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;

import core.metamodel.attribute.IValueSpace;
import core.metamodel.value.numeric.ContinuousValue;

@JsonTypeName(DoubleValueAggregator.SELF)
public class DoubleValueAggregator implements IAggregatorValueFunction<ContinuousValue> {

	public static final String SELF = IAggregatorValueFunction.DEFAULT_TAG+"DOUBLE AGGREGATOR";
	private static final DoubleValueAggregator INSTANCE = new DoubleValueAggregator();
	
	private DoubleValueAggregator() {}
	
	@JsonCreator
	public static DoubleValueAggregator getInstance() {
		return INSTANCE;
	}
	
	@Override
	public ContinuousValue transpose(Collection<ContinuousValue> values, IValueSpace<ContinuousValue> valueSpace) {
		return valueSpace.proposeValue(Double.toString(values.stream()
				.mapToDouble(v -> v.getActualValue()).sum()));
	}

	@Override
	public String getType() {
		return SELF;
	}
	
}
