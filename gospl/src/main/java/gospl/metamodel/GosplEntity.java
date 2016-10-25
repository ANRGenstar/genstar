package gospl.metamodel;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import core.io.survey.attribut.ASurveyAttribute;
import core.io.survey.attribut.value.AValue;
import core.metamodel.IEntity;

public class GosplEntity implements IEntity<ASurveyAttribute, AValue> {

	private Map<ASurveyAttribute, AValue> attributes;

	public GosplEntity(Map<ASurveyAttribute, AValue> attributes){
		this.attributes = attributes;
	}
	
	@Override
	public Collection<ASurveyAttribute> getAttributes() {
		return attributes.keySet();
	}

	@Override
	public AValue getValueForAttribute(ASurveyAttribute attribute) {
		return attributes.get(attribute);
	}

	@Override
	public Collection<AValue> getValues() {
		return Collections.unmodifiableCollection(attributes.values());
	}

}
