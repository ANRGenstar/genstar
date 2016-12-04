package gospl.entity.attribute;

import core.metamodel.pop.APopulationAttribute;
import core.util.data.GSEnumDataType;

public class UniqueAttribute extends APopulationAttribute {

	public UniqueAttribute(String name, GSEnumDataType dataType) {
		super(name, dataType);
	}

	@Override
	public boolean isRecordAttribute() {
		return false;
	}

}
