package core.metamodel.attribute.emergent.function.aggregator;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonTypeName;

import core.metamodel.attribute.IAttribute;
import core.metamodel.value.numeric.IntegerValue;

/**
 * Sum up the integer values as the aggregated process
 * 
 * @author kevinchapuis
 *
 */
@JsonTypeName(IntegerSumValueFunction.SELF)
public class IntegerSumValueFunction implements IAggregateValueFunction<IntegerValue, IntegerValue> {

	public static final String SELF = "INTEGER AGGREGATOR";
	
	private IAttribute<IntegerValue> referent;
	
	public IntegerSumValueFunction(IAttribute<IntegerValue> referent) {
		this.referent = referent;
	}
	
	@Override
	public IntegerValue aggregate(Collection<IntegerValue> values) {
		return referent.getValueSpace().proposeValue(Integer.toString(values.stream()
				.mapToInt(v -> v.getActualValue()).sum()));
	}

	@Override
	public IAttribute<IntegerValue> getReferentAttribute() {
		return this.referent;
	}

}
