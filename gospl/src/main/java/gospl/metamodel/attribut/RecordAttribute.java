package gospl.metamodel.attribut;

import gospl.metamodel.attribut.value.IValue;
import io.util.data.GSDataType;

public class RecordAttribute extends AbstractAttribute implements IAttribute {

	private IValue recordValue;
	
	public RecordAttribute(String name, GSDataType dataType, IAttribute referentAttribute) {
		super(name, dataType, referentAttribute);
	}

	@Override
	public boolean isRecordAttribute() {
		return true;
	}

	@Override
	public IValue findMatchingAttributeValue(IValue val) {
		if(val.equals(recordValue))
			return recordValue;
		return null;
	}

}
