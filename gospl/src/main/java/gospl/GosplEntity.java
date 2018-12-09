package gospl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import core.metamodel.attribute.Attribute;
import core.metamodel.attribute.EmergentAttribute;
import core.metamodel.attribute.IAttribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.entity.IEntity;
import core.metamodel.value.IValue;
import core.metamodel.value.numeric.IntegerValue;

/**
 * A GoSPL Entity is a population entity
 * 
 * TODO if an entity is removed, remove references to it in other agents !
 *
 */
public class GosplEntity extends ADemoEntity {
	
	private EmergentAttribute<IntegerValue, Collection<IEntity<? extends IAttribute<? extends IValue>>>, ?> sizeAttribute;
	
	public GosplEntity(Map<Attribute<? extends IValue>, IValue> attributes){
		super(attributes);
	}
	
	public GosplEntity(Map<Attribute<? extends IValue>, IValue> attributes, 
			EmergentAttribute<IntegerValue, Collection<IEntity<? extends IAttribute<? extends IValue>>>, ?> sizeAttribute) {
		super(attributes);
		this.sizeAttribute = sizeAttribute;
	}

	public GosplEntity(){
		super();
	}
	
	@Override
	public GosplEntity clone(){
		return new GosplEntity(new HashMap<>(this.getAttributeMap()));
	}

	@Override
	public IntegerValue getCountChildren() {
		return sizeAttribute.getEmergentValue(this);
	}


}
