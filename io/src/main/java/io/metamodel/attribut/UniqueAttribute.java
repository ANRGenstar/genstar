package io.metamodel.attribut;

import io.util.data.GSEnumDataType;

public class UniqueAttribute extends AbstractAttribute implements IAttribute {

	public UniqueAttribute(String name, GSEnumDataType dataType) {
		super(name, dataType);
	}

	@Override
	public boolean isRecordAttribute() {
		return false;
	}

}
