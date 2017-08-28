package gospl.entity.attribute;

import core.util.data.GSEnumDataType;

public enum GSEnumAttributeType {

	unique,
	range,
	record;
	
	public String getDefaultStringValue(GSEnumDataType dataType){
		if (dataType == null)
			return "";
		
		String def = "";
		switch (this) {
		case unique:
			def = dataType.getDefaultValue();
			break;
		case range:
			def = dataType.getDefaultValue()+":"+dataType.getDefaultValue();
		case record:
			def = "null";
		default:
			def = "";
			break;
		}
		return def;
	}
	
}
