package gospl.metamodel.attribut;

import io.data.GSDataType;

public enum GosplValueType {

	unique,
	range,
	record;
	
	public String getDefaultStringValue(GSDataType dataType){
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
