package gospl.metamodel;

import java.util.Collection;
import java.util.Map;

import gospl.metamodel.attribut.IAttribute;
import gospl.metamodel.attribut.value.IValue;
import gospl.metamodel.exception.UndefinedAttributeException;

public class GosplEntity implements IEntity {

	private Map<IAttribute, IValue> attributes;

	public GosplEntity(Map<IAttribute, IValue> attributes){
		this.attributes = attributes;
	}
	
	@Override
	public Collection<IAttribute> getAttributes() {
		return attributes.keySet();
	}

	@Override
	public IValue getValueForAttribute(IAttribute attribute) throws UndefinedAttributeException {
		return attributes.get(attribute);
	}

}
