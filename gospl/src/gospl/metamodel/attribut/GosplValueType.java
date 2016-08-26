package gospl.metamodel.attribut;

import io.datareaders.DataType;

public enum GosplValueType {

	unique,
	range,
	record;
	
	public String getDefaultStringValue(DataType dataType){
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
