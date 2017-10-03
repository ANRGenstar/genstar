package core.metamodel.value.categoric;

import core.metamodel.IAttribute;
import core.metamodel.value.IValue;
import core.util.data.GSEnumDataType;

public class NominalValue implements IValue {
	
	private String value;
	private IAttribute attribute;

	@Override
	public GSEnumDataType getType() {
		return GSEnumDataType.String;
	}

	@Override
	public String getStringValue() {
		return value;
	}

	@Override
	public IAttribute getAttribute() {
		return attribute;
	}

}
