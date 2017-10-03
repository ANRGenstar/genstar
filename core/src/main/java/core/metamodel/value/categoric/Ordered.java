package core.metamodel.value.categoric;

import java.util.List;

import core.metamodel.value.IValueSpace;
import core.util.data.GSEnumDataType;

public class Ordered implements IValueSpace<String> {

	List<String> values;
	
	@Override
	public String getValue(String value) {
		return value;
	}
	
	public int compare(String referent, String compareTo) {
		int valRef = values.indexOf(referent);
		int valComp = values.indexOf(compareTo);
		return valRef > valComp ? -1 : valRef < valComp ? 1 : 0;
	}

	@Override
	public GSEnumDataType getType() {
		return GSEnumDataType.String;
	}

}
