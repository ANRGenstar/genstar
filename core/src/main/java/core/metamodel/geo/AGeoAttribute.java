package core.metamodel.geo;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import core.metamodel.IAttribute;


/**
 * TODO: javadoc
 * 
 * @author kevinchapuis
 *
 */
public abstract class AGeoAttribute implements IAttribute<AGeoValue> {

	private String name;
	
	private Map<String, AGeoValue> values;
	private AGeoValue emptyValue;

	public AGeoAttribute(Set<AGeoValue> values, AGeoValue emptyValue, String name) {
		this.values = new ConcurrentHashMap<String, AGeoValue>();
		for (AGeoValue v : values) this.values.put(v.getInputStringValue(), v);
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
		return Collections.unmodifiableSet(new HashSet<AGeoValue>(values.values()));
	}
	
	@Override
	public boolean setValues(Set<AGeoValue> values){
		if(this.values.isEmpty()) {
			for (AGeoValue v: values)addValue(v);
			return true;
		}
			
		return false;
	}
	
	public void addValue(AGeoValue value){
		values.put(value.getInputStringValue(), value);
	}
	
	public AGeoValue getValue(String name){
		return values.get(name);
	}
	

	@Override
	public boolean hasValue(String name) {
		return values.containsKey(name);
	}

}
