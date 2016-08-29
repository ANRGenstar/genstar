package gospl.metamodel.attribut;

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
		Set<IValue> vals = new HashSet<>(values);
		if(emptyValue != null)
			vals.add(emptyValue);
		return vals;
	}

	@Override
	public boolean setValues(Set<IValue> values) {
		if(values.isEmpty()){
			values = new HashSet<>(values);
			return true;
		}
		return false;
	}

	@Override
	public void setEmptyValue(IValue emptyValue) {
		this.emptyValue = emptyValue;
	}
	
	@Override
	public abstract boolean isRecordAttribute();

}
