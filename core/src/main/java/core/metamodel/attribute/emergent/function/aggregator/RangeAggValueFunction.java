package core.metamodel.attribute.emergent.function.aggregator;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonTypeName;

import core.metamodel.attribute.IAttribute;
import core.metamodel.value.numeric.RangeSpace;
import core.metamodel.value.numeric.RangeValue;
import core.metamodel.value.numeric.template.GSRangeTemplate;


/**
 * Sums up the bottom and top bound of each range value as the aggregated process. Makes the
 * implicit assumption that all provided ranges have significant max and min value to be sumed up.
 * In particular maximum bound are usually hard to define (like maximum wage or age)
 * 
 * @author kevinchapuis
 *
 */
@JsonTypeName(RangeAggValueFunction.SELF)
public class RangeAggValueFunction implements IAggregateValueFunction<RangeValue, RangeValue> {

	public static final String SELF = "RANGE AGGREGATOR";
	
	private IAttribute<RangeValue> referent;
	private GSRangeTemplate rt;
	
	public RangeAggValueFunction(IAttribute<RangeValue> referent) {
		this.referent = referent;
		this.rt = ((RangeSpace) referent.getValueSpace()).getRangeTemplate();
	}
	
	@Override
	public RangeValue aggregate(Collection<RangeValue> values) {
		Number bottom = values.stream().map(r -> r.getBottomBound())
				.reduce(0, (b1, b2) -> this.add(b1, b2));
		Number top = values.stream().map(r -> r.getTopBound())
				.reduce(0, (b1, b2) -> this.add(b1, b2));
		return referent.getValueSpace().getInstanceValue(rt.getMiddleTemplate(bottom, top));
	}
	
	private Number add(Number n1, Number n2) {
		if(n1.getClass().equals(Integer.class)
				&& n2.getClass().equals(Integer.class))
			return n1.intValue() + n2.intValue();
		return n1.doubleValue() + n2.doubleValue();		
	}

	@Override
	public IAttribute<RangeValue> getReferentAttribute() {
		return this.referent;
	}

}
