package core.metamodel.value.categoric;

import core.metamodel.value.IValue;
import core.util.data.GSEnumDataType;

public class NominalValue implements IValue {
	
	private String value;
	private NominalSpace vs;
	
	protected NominalValue(NominalSpace vs, String value){
		this.value = value;
		this.vs = vs;
	}

	@Override
	public GSEnumDataType getType() {
		return GSEnumDataType.Nominal;
	}

	@Override
	public String getStringValue() {
		return value;
	}

	@Override
	public NominalSpace getValueSpace() {
		return vs;
	}

}
