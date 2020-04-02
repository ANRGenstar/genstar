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
		GosplEntity clone = new GosplEntity(new HashMap<>(this.getAttributeMap()));
		if (this.hasChildren()) {
			clone.addChildren(this.getChildren());
			clone.getChildren().stream().forEach(child -> child.setParent(clone));
		}
		if (this.hasParent()) {
			clone.setParent(this.getParent());
			this.getParent().getChildren().remove(this);
			this.getParent().getChildren().add(clone);
		}
		clone.setWeight(this.getWeight());
		return clone;
	}


}
