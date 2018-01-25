package core.metamodel.attribute.emergent.function.aggregator;

import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

import core.metamodel.attribute.IAttribute;
import core.metamodel.attribute.IValueSpace;
import core.metamodel.value.categoric.OrderedSpace;
import core.metamodel.value.categoric.OrderedValue;
import core.metamodel.value.categoric.template.GSCategoricTemplate;

/**
 * Function that aggregate ordered value into ordered value in a dynamic way, based on inner ordered value.
 * </p>
 * The method to compute aggregated order that drive comparison between ordered value is as follow: </br>
 * computed mean order of values plus adding the suite of orders (from highest to smallest) to the digital part
 * 
 * @author kevinchapuis
 *
 */
public class OrderedAggValueFunction implements IAggregateValueFunction<OrderedValue, OrderedValue> {

	private OrderedSpace os;
	
	public OrderedAggValueFunction(IAttribute<OrderedValue> attribute) {
		this.os = new OrderedSpace(attribute, new GSCategoricTemplate());
	}
	
	@Override
	public OrderedValue aggregate(Collection<OrderedValue> values) {
		return os.addValue(this.getAggregate(values), 
				values.stream().map(v -> v.getStringValue()).collect(Collectors.joining(";")));
	}

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
	public IValueSpace<OrderedValue> getValueSpace() {
		return this.os;
	}
	
}
