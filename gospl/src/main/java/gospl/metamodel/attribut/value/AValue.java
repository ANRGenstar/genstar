package gospl.metamodel.attribut.value;

import gospl.metamodel.attribut.IAttribute;
import io.util.data.GSDataType;

public abstract class AValue implements IValue {

	private GSDataType dataType;
	private IAttribute attribute;
	
	private String inputStringValue;
	private String mappedStringValue;
	
	public AValue(String inputStringValue, String mappedStringValue, GSDataType dataType, IAttribute attribute){
		this.inputStringValue = inputStringValue;
		this.mappedStringValue = mappedStringValue;
		this.dataType = dataType;
		this.attribute = attribute;
	}
	
	public AValue(String inputStringValue, GSDataType dataType, IAttribute attribute) {
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
	public IAttribute getAttribute() {
		return attribute;
	}

	@Override
	public GSDataType getDataType() {
		return dataType;
	}
	
	@Override
	public String toString(){
		return mappedStringValue+" ("+attribute.getName()+")";
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
		AValue other = (AValue) obj;
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
