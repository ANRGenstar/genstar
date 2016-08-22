package spll.algo.variable;

public class SPLRawVariable implements ISPLVariable<Object> {

	private Object value;
	private String name;
	
	protected SPLRawVariable(Object value, String name) {
		this.value = value;
		this.name = name;
	}
	
	@Override
	public Object getValue() {
		return value;
	}
	
	@Override
	public String getStringValue() {
		return getValue().toString();
	}
	
	@Override
	public String getName(){
		return name;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		result = prime * result + ((this.getStringValue() == null) ? 0 : this.getStringValue().hashCode());
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
		SPLRawVariable other = (SPLRawVariable) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		if (getStringValue() == null) {
			if (other.getStringValue() != null)
				return false;
		} else if (!getStringValue().equals(other.getStringValue()))
			return false;
		return true;
	}



}
