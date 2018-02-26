package core.metamodel.attribute.emergent.aggregator;

import java.util.Collection;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonTypeName;

import core.metamodel.attribute.IValueSpace;
import core.metamodel.value.categoric.NominalValue;

@JsonTypeName(NominalValueAggregator.SELF)
public class NominalValueAggregator implements IAggregatorValueFunction<NominalValue> {

	public static final String SELF = IAggregatorValueFunction.DEFAULT_TAG+"NOMINAL AGGREGATOR";
	private static final NominalValueAggregator INSTANCE = new NominalValueAggregator();
	
	private NominalValueAggregator() {}
	
	public static NominalValueAggregator getInstance() {
		return INSTANCE;
	}
	
	@Override
	public NominalValue transpose(Collection<NominalValue> values, IValueSpace<NominalValue> vs) {
		return vs.getInstanceValue(values.stream()
				.map(v -> v.getStringValue()).collect(Collectors.joining(getDefaultCharConcat())));
	}

	@Override
	public String getType() {
		return SELF;
	}
	
}
