package core.metamodel.value.categoric;

import core.metamodel.value.IValueSpace;
import core.util.data.GSEnumDataType;

public class Category implements IValueSpace<String> {

	@Override
	public String getValue(String value) {
		return value;
	}

	@Override
	public GSEnumDataType getType() {
		return GSEnumDataType.String;
	}

}
