package core.metamodel.pop;

import core.metamodel.IValue;
import core.util.data.GSEnumDataType;

public abstract class APopulationValue implements IValue {

	private GSEnumDataType dataType;
	private APopulationAttribute attribute;
	
	private String inputStringValue;
	private String mappedStringValue;
	
	public APopulationValue(String inputStringValue, String mappedStringValue, GSEnumDataType dataType, APopulationAttribute attribute){
		this.inputStringValue = inputStringValue;
		this.mappedStringValue = mappedStringValue;
		this.dataType = dataType;
		this.attribute = attribute;
	}
	
	public APopulationValue(String inputStringValue, GSEnumDataType dataType, APopulationAttribute attribute) {
		this(inputStringValue, inputStringValue, dataType, attribute);
	}
	
	@Override
	public String getStringValue() {
		return mappedStringValue;
	}

	@Override
	public String getInputStringValue() {
		return inputStringValue;
	}

	@Override
	public APopulationAttribute getAttribute() {
		return attribute;
	}

	public GSEnumDataType getDataType() {
		return dataType;
	}
	
	@Override
	public String toString(){
		return mappedStringValue+" ("+attribute.getAttributeName()+")";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		// Did not keep attribute.hashCode() to avoid recursive call
		// result = prime * result + ((attribute == null) ? 0 : attribute.hashCode());
		result = prime * result + ((dataType == null) ? 0 : dataType.hashCode());
		result = prime * result + ((inputStringValue == null) ? 0 : inputStringValue.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		APopulationValue other = (APopulationValue) obj;
		if (attribute == null) {
			if (other.attribute != null)
				return false;
		} else if (!attribute.equals(other.attribute))
			return false;
		if (dataType != other.dataType)
			return false;
		if (inputStringValue == null) {
			if (other.inputStringValue != null)
				return false;
		} else if (!inputStringValue.equals(other.inputStringValue))
			return false;
		return true;
	}

}
