package gospl.metamodel.attribut;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import gospl.metamodel.attribut.value.IValue;
import io.data.GSDataType;

public abstract class AbstractAttribute implements IAttribute {

	private IAttribute referentAttribute;
	private String name;
	private GSDataType dataType;
	
	private Set<IValue> values = new HashSet<>();
	private IValue emptyValue;
	
	public AbstractAttribute(String name, GSDataType dataType, IAttribute referentAttribute) {
		this.name = name;
		this.dataType = dataType;
		this.referentAttribute = referentAttribute;
	}

	public AbstractAttribute(String name, GSDataType dataType) {
		this.name = name;
		this.dataType = dataType;
		this.referentAttribute = this;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public GSDataType getDataType() {
		return dataType;
	}

	@Override
	public IAttribute getReferentAttribute() {
		return referentAttribute;
	}

	@Override
	public Set<IValue> getValues() {
		return Collections.unmodifiableSet(values);
	}

	@Override
	public boolean setValues(Set<IValue> values) {
		if(this.values.isEmpty())
			return this.values.addAll(values);
		return false;
	}
	
	@Override
	public IValue findMatchingAttributeValue(IValue val) {
		if(values.contains(val))
			return val;
		return null;
	}
	
	@Override
	public IValue getEmptyValue() {
		return emptyValue;
	}

	@Override
	public void setEmptyValue(IValue emptyValue) {
		this.emptyValue = emptyValue;
	}
	
	@Override
	public abstract boolean isRecordAttribute();
	
	@Override
	public String toString(){
		return name+" ("+dataType+") - "+values.size()+" values";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dataType == null) ? 0 : dataType.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((values == null) ? 0 : values.size());
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
		AbstractAttribute other = (AbstractAttribute) obj;
		if (dataType != other.dataType)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (values == null) {
			if (other.values != null)
				return false;
		} else if (values.size() != other.values.size())
			return false;
		return true;
	}

}
