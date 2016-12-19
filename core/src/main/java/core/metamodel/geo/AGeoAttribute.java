package core.metamodel.geo;

import java.util.Collections;
import java.util.Set;

import core.metamodel.IAttribute;


/**
 * TODO: javadoc
 * 
 * @author kevinchapuis
 *
 */
public abstract class AGeoAttribute implements IAttribute<AGeoValue> {

	private String name;
	
	private Set<AGeoValue> values;
	private AGeoValue emptyValue;

	public AGeoAttribute(Set<AGeoValue> values, AGeoValue emptyValue, String name) {
		this.values = values;
		this.emptyValue = emptyValue;
		this.name = name;
	}
	
	public AGeoAttribute(Set<AGeoValue> values, String name) {
		this(values, null, name);
	}

	@Override
	public String getAttributeName() {
		return name;
	}

	@Override
	public void setEmptyValue(AGeoValue emptyValue) {
		this.emptyValue = emptyValue;
	}

	@Override
	public AGeoValue getEmptyValue() {
		return emptyValue;
	}

	@Override
	public Set<AGeoValue> getValues() {
		return Collections.unmodifiableSet(values);
	}
	
	@Override
	public boolean setValues(Set<AGeoValue> values){
		if(this.values.isEmpty())
			return this.values.addAll(values);
		return false;
	}
	
	public boolean addValue(AGeoValue value){
		return values.add(value);
	}
	
}
