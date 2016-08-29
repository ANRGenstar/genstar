package gospl.metamodel.attribut;

import io.data.GSDataType;

public class RecordeAttribute extends AbstractAttribute implements IAttribute {

	public RecordeAttribute(String name, GSDataType dataType, IAttribute referentAttribute) {
		super(name, dataType, referentAttribute);
	}

	@Override
	public boolean isRecordAttribute() {
		return true;
	}

}
