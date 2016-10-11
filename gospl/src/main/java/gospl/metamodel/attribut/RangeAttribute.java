package gospl.metamodel.attribut;

import io.util.data.GSDataType;

public class RangeAttribute extends AbstractAttribute implements IAttribute {

	public RangeAttribute(String name, GSDataType dataType) {
		super(name, dataType);
	}

	@Override
	public boolean isRecordAttribute() {
		return false;
	}

}
