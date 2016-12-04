package spll.entity.attribute;

import java.util.HashSet;
import java.util.Set;

import core.metamodel.geo.AGeoAttribute;
import core.metamodel.geo.AGeoValue;

public class RawGeoAttribute extends AGeoAttribute {

	public RawGeoAttribute(Set<AGeoValue> values, AGeoValue emptyValue, String name) {
		super(values, emptyValue, name);
	}
	
	public RawGeoAttribute(Set<AGeoValue> values, String name){
		this(values, null, name);
	}
	
	public RawGeoAttribute(String name){
		this(new HashSet<>(), null, name);
	}


}
