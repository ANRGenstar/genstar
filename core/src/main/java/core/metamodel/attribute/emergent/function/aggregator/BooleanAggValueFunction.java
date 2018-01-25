package core.metamodel.attribute.emergent.function.aggregator;

import java.util.Collection;

import core.metamodel.attribute.IValueSpace;
import core.metamodel.value.binary.BooleanValue;

/**
 * Aggregate boolean values according to proposed aggregation style: </p>
 * {@link BooleanAggregationStyle#ALL} : (default) Return true if and only if all values are true </br>
 * {@link BooleanAggregationStyle#MOST} : Return true if there is at least 50% of true value </br>
 * {@link BooleanAggregationStyle#ATLEASTONE} : Return true if at least one value is true </br>
 * 
 * @author kevinchapuis
 *
 */
public class BooleanAggValueFunction implements IAggregateValueFunction<BooleanValue, BooleanValue> {

	public static enum BooleanAggregationStyle {MOST, ALL, ATLEASTONE}
	
	private IValueSpace<BooleanValue> bs;
	private BooleanAggregationStyle bas = BooleanAggregationStyle.ALL;
	
	public BooleanAggValueFunction(IValueSpace<BooleanValue> bs) {
		this.bs = bs;
	}
	
	@Override
	public BooleanValue aggregate(Collection<BooleanValue> values) {
		switch (bas) {
		case MOST:
			return this.most(values);
		case ATLEASTONE:
			return this.atLeastOne(values);
		default:
			return this.all(values);
		}
		
	}
	
	@Override
	public IValueSpace<BooleanValue> getValueSpace() {
		return this.bs;
	}
	
	public void setAggregationStyle(BooleanAggregationStyle style) {
		this.bas = style;
	}

	private BooleanValue all(Collection<BooleanValue> values) {
		return bs.getValue(Boolean.toString(values.stream().anyMatch(v -> !v.getActualValue())));
	}
	
	private BooleanValue most(Collection<BooleanValue> values) {
		return bs.getValue(Boolean.toString(values.stream()
				.filter(v -> v.getActualValue()).count() >= values.size() / 2d));
	}
	
	private BooleanValue atLeastOne(Collection<BooleanValue> values) {
		return bs.getValue(Boolean.toString(values.stream().anyMatch(v -> v.getActualValue())));
	}
	
}
