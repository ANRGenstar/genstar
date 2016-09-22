package gospl.metamodel.attribut.value;

import gospl.metamodel.attribut.GosplValueType;
import gospl.metamodel.attribut.IAttribute;
import io.data.GSDataType;

public class UniqueValue extends AValue {

	public UniqueValue(String inputStringValue, String mappedStringValue, GSDataType dataType, IAttribute attribute){
		super(inputStringValue, mappedStringValue, dataType, attribute);
	}
	
	public UniqueValue(String inputStringValue, GSDataType dataType, IAttribute attribute) {
		super(inputStringValue, dataType, attribute);
	}
	
	public UniqueValue(GSDataType dataType, IAttribute attribute) {
		this(GosplValueType.unique.getDefaultStringValue(dataType), dataType, attribute);
	}

}
