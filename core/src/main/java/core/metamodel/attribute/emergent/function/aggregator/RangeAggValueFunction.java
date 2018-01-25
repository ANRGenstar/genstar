package core.metamodel.attribute.emergent.function.aggregator;

import java.util.Collection;

import core.metamodel.attribute.IValueSpace;
import core.metamodel.value.numeric.RangeSpace;
import core.metamodel.value.numeric.RangeValue;


/**
 * Sums up the bottom and top bound of each range value as the aggregated process. Makes the
 * implicit assumption that all provided ranges have significant max and min value to be sumed up.
 * In particular maximum bound are usually hard to define (like maximum wage or age)
 * 
 * @author kevinchapuis
 *
 */
public class RangeAggValueFunction implements IAggregateValueFunction<RangeValue, RangeValue> {

	private RangeSpace rs;
	
	public RangeAggValueFunction(RangeSpace rs) {
		this.rs = rs;
	}
	
	@Override
	public RangeValue aggregate(Collection<RangeValue> values) {
		Number bottom = values.stream().map(r -> r.getBottomBound())
				.reduce(0, (b1, b2) -> this.add(b1, b2));
		Number top = values.stream().map(r -> r.getTopBound())
				.reduce(0, (b1, b2) -> this.add(b1, b2));
		return rs.getInstanceValue(rs.getRangeTemplate().getMiddleTemplate(bottom, top));
	}
	
	private Number add(Number n1, Number n2) {
		if(n1.getClass().equals(Integer.class)
				&& n2.getClass().equals(Integer.class))
			return n1.intValue() + n2.intValue();
		return n1.doubleValue() + n2.doubleValue();		
	}

	@Override
	public IValueSpace<RangeValue> getValueSpace() {
		return this.rs;
	}

}
