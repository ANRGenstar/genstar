package spll.entity.attribute;

import java.util.HashSet;
import java.util.Set;

import core.metamodel.geo.AGeoAttribute;
import core.metamodel.value.geo.IValue;

/**
 * Attribute of geo entity that are not specified with any content type:
 * this attribute does not give any clue on which type of data it encapsulate.
 * You can just ask for string view of the value
 * <p>
 * TODO: move to a spectified type of data, either Numeric or Categoric
 * 
 * @author kevinchapuis
 *
 */
public class RawGeoAttribute extends AGeoAttribute {

	public RawGeoAttribute(Set<IValue> values, IValue emptyValue, String name) {
		super(values, emptyValue, name);
	}
	
	public RawGeoAttribute(Set<IValue> values, String name){
		this(values, null, name);
	}
	
	public RawGeoAttribute(String name){
		this(new HashSet<>(), null, name);
	}


}
