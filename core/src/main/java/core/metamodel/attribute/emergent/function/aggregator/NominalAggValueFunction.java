package core.metamodel.attribute.emergent.function.aggregator;

import java.util.Collection;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonTypeName;

import core.metamodel.attribute.IAttribute;
import core.metamodel.value.categoric.NominalValue;

@JsonTypeName(NominalAggValueFunction.SELF)
public class NominalAggValueFunction implements IAggregateValueFunction<NominalValue, NominalValue> {

	public static final String SELF = "NOMINAL AGGREGATOR";
	
	private IAttribute<NominalValue> referent;
	
	private String aggChar = "-";
	
	public NominalAggValueFunction(IAttribute<NominalValue> referent) {
		this.referent = referent;
	}
	
	@Override
	public NominalValue aggregate(Collection<NominalValue> values) {
		return referent.getValueSpace().getInstanceValue(values.stream()
				.map(v -> v.getStringValue()).collect(Collectors.joining(aggChar)));
	}
	
	public void setAggChar(String aggChar) {
		this.aggChar = aggChar;
	}

	@Override
	public IAttribute<NominalValue> getReferentAttribute() {
		return this.referent;
	}

}
