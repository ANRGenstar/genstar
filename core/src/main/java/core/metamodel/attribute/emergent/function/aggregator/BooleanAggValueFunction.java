package core.metamodel.attribute.emergent.function.aggregator;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonTypeName;

import core.metamodel.attribute.IAttribute;
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
@JsonTypeName(BooleanAggValueFunction.SELF)
public class BooleanAggValueFunction implements IAggregateValueFunction<BooleanValue, BooleanValue> {

	public static final String SELF = "BOOLEAN AGGREGATOR"; 
	
	public static enum BooleanAggregationStyle {MOST, ALL, ATLEASTONE}
	
	private IAttribute<BooleanValue> referent;
	private BooleanAggregationStyle bas = BooleanAggregationStyle.ALL;
	
	public BooleanAggValueFunction(IAttribute<BooleanValue> referent) {
		this.referent = referent;
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
	public IAttribute<BooleanValue> getReferentAttribute() {
		return this.referent;
	}
	
	public void setAggregationStyle(BooleanAggregationStyle style) {
		this.bas = style;
	}

	private BooleanValue all(Collection<BooleanValue> values) {
		return this.referent.getValueSpace().getValue(Boolean
				.toString(values.stream().anyMatch(v -> !v.getActualValue())));
	}
	
	private BooleanValue most(Collection<BooleanValue> values) {
		return this.referent.getValueSpace().getValue(Boolean.toString(values.stream()
				.filter(v -> v.getActualValue()).count() >= values.size() / 2d));
	}
	
	private BooleanValue atLeastOne(Collection<BooleanValue> values) {
		return this.referent.getValueSpace().getValue(Boolean
				.toString(values.stream().anyMatch(v -> v.getActualValue())));
	}
	
}
