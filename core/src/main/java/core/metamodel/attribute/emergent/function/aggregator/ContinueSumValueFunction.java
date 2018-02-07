package core.metamodel.attribute.emergent.function.aggregator;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonTypeName;

import core.metamodel.attribute.IAttribute;
import core.metamodel.value.numeric.ContinuousValue;

/**
 * Sums up the continuous value as the aggregated process
 * 
 * @author kevinchapuis
 *
 */
@JsonTypeName(ContinueSumValueFunction.SELF)
public class ContinueSumValueFunction implements IAggregateValueFunction<ContinuousValue, ContinuousValue> {

	public static final String SELF = "CONTINUOUS AGGREGATOR"; 
	
	public IAttribute<ContinuousValue> referent;
	
	public ContinueSumValueFunction(IAttribute<ContinuousValue> referent) {
		this.referent = referent;
	}
	
	@Override
	public ContinuousValue aggregate(Collection<ContinuousValue> values) {
		return referent.getValueSpace().proposeValue(Double.toString(values.stream()
				.mapToDouble(v -> v.getActualValue()).sum()));
	}

	@Override
	public IAttribute<ContinuousValue> getReferentAttribute() {
		return this.referent;
	}

}
