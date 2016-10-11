package gospl.metamodel.attribut.value;

import gospl.metamodel.attribut.GosplValueType;
import gospl.metamodel.attribut.IAttribute;
import io.util.data.GSDataType;

public class RangeValue extends AValue {
	
	private String inputStringLowerBound;
	private String inputStringUpperBound;

	public RangeValue(String inputStringLowerBound, String inputStringUpperBound, String inputStringValue, GSDataType dataType, IAttribute attribute) {
		super(inputStringValue, dataType, attribute);
		this.inputStringLowerBound = inputStringLowerBound;
		this.inputStringUpperBound = inputStringUpperBound;
	}
	
	public RangeValue(GSDataType dataType, IAttribute attribute) {
		this(GosplValueType.unique.getDefaultStringValue(dataType), GosplValueType.unique.getDefaultStringValue(dataType), 
				GosplValueType.range.getDefaultStringValue(dataType), dataType, attribute);
	}
	
	public String getInputStringLowerBound(){
		return inputStringLowerBound;
	}
	
	public String getInputStringUpperBound(){
		return inputStringUpperBound;
	}

}
