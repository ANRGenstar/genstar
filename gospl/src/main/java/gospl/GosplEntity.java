package gospl;

import java.util.HashMap;
import java.util.Map;

import core.metamodel.attribute.Attribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.value.IValue;

/**
 * A GoSPL Entity is a population entity
 * 
 * TODO if an entity is removed, remove references to it in other agents !
 *
 */
public class GosplEntity extends ADemoEntity {
	
	public GosplEntity(Map<Attribute<? extends IValue>, IValue> attributes){
		super(attributes);
	}
	
	public GosplEntity(Map<Attribute<? extends IValue>, IValue> attributes, double weight) {
		super(attributes);
		super.setWeight(weight);
	}

	public GosplEntity(){
		super();
	}
	
	@Override
	public GosplEntity clone(){
		return new GosplEntity(new HashMap<>(this.getAttributeMap()));
	}


}
