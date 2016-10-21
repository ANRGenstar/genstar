package io.metamodel.attribut;

import io.util.data.GSEnumDataType;

public class RangeAttribute extends AbstractAttribute implements IAttribute {

	public RangeAttribute(String name, GSEnumDataType dataType) {
		super(name, dataType);
	}

	@Override
	public boolean isRecordAttribute() {
		return false;
	}

}
