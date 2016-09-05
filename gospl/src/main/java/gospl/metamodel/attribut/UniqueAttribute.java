package gospl.metamodel.attribut;

import io.data.GSDataType;

public class UniqueAttribute extends AbstractAttribute implements IAttribute {

	public UniqueAttribute(String name, GSDataType dataType) {
		super(name, dataType);
	}

	@Override
	public boolean isRecordAttribute() {
		return false;
	}

}
