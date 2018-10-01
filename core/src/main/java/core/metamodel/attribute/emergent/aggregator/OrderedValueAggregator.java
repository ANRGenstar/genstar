package core.metamodel.attribute.emergent.aggregator;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonTypeName;

import core.metamodel.value.IValueSpace;
import core.metamodel.value.categoric.OrderedSpace;
import core.metamodel.value.categoric.OrderedValue;

@JsonTypeName(OrderedValueAggregator.SELF)
public class OrderedValueAggregator implements IAggregatorValueFunction<OrderedValue> {

	public static final String SELF = IAggregatorValueFunction.DEFAULT_TAG+"ORDERED VALUE AGGREGATOR";
	private static final OrderedValueAggregator INSTANCE = new OrderedValueAggregator();
	
	private OrderedValueAggregator() {}
	
	public static OrderedValueAggregator getInstance() {
		return INSTANCE;
	}
	
	@Override
	public OrderedValue transpose(Collection<OrderedValue> values, IValueSpace<OrderedValue> vs) {
		return ((OrderedSpace)vs).addValue(this.getAggregate(values), 
				values.stream().map(v -> v.getStringValue()).collect(Collectors.joining(this.getDefaultCharConcat())));
	}
	
	@Override
	public Collection<OrderedValue> reverse(OrderedValue value, IValueSpace<OrderedValue> valueSpace) {
		return Arrays.asList(value.getStringValue().split(this.getDefaultCharConcat().toString())).stream()
				.map(v -> valueSpace.getValue(v))
				.sorted(new Comparator<OrderedValue>() {
					@Override
					public int compare(OrderedValue o1, OrderedValue o2) {
						return o1.getOrder().doubleValue() > o2.getOrder().doubleValue() ? -1 : 
							o1.getOrder().doubleValue() < o2.getOrder().doubleValue() ? 1 : 0;
					}
				})
				.collect(Collectors.toList());
	}
	
	/*
	 * 
	 */
	private double getAggregate(Collection<OrderedValue> values) {
		double mean = values.stream().mapToDouble(v -> v.getOrder().doubleValue()).sum() / values.size();
		return Math.floor(mean) + (mean - Math.floor(mean) + 
				Double.valueOf("0."+values.stream().map(v -> 
					(int) Math.round(v.getOrder().doubleValue())).sorted(
							new Comparator<Integer>() {@Override
								public int compare(Integer o1, Integer o2) {return o1 > o2 ? -1 : o1 < o2 ? 1 : 0;}
							})
						.map(i -> i.toString()).collect(Collectors.joining())));
	}

	@Override
	public String getType() {
		return SELF;
	}
	
}
