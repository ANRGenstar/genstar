package core.metamodel.attribute.emergent.function.aggregator;

import java.util.Collection;
import java.util.stream.Collectors;

import core.metamodel.attribute.IValueSpace;
import core.metamodel.value.categoric.NominalValue;

public class NominalAggValueFunction implements IAggregateValueFunction<NominalValue, NominalValue> {

	private IValueSpace<NominalValue> ns;
	
	private String aggChar = "-";
	
	public NominalAggValueFunction(IValueSpace<NominalValue> ns) {
		this.ns = ns;
	}
	
	@Override
	public NominalValue aggregate(Collection<NominalValue> values) {
		return ns.getInstanceValue(values.stream().map(v -> v.getStringValue()).collect(Collectors.joining(aggChar)));
	}
	
	public void setAggChar(String aggChar) {
		this.aggChar = aggChar;
	}

	@Override
	public IValueSpace<NominalValue> getValueSpace() {
		return this.ns;
	}

}
