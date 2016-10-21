package gospl.metamodel;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import io.metamodel.IEntity;
import io.metamodel.attribut.IAttribute;
import io.metamodel.attribut.value.IValue;
import io.metamodel.exception.UndefinedAttributeException;

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

	@Override
	public Collection<IValue> getValues() {
		return Collections.unmodifiableCollection(attributes.values());
	}

}
