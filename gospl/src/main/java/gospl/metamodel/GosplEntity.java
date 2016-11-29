package gospl.metamodel;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import com.vividsolutions.jts.geom.Point;

import core.io.survey.attribut.ASurveyAttribute;
import core.io.survey.attribut.value.AValue;
import core.metamodel.IEntity;

public class GosplEntity implements IEntity<ASurveyAttribute, AValue> {

	private Point location = null;
	private IEntity<?,?> nest = null;
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

	@Override
	public Point getLocation() {
		return location;
	}

	@Override
	public IEntity<?, ?> getNest() {
		return nest;
	}

	@Override
	public void setLocation(Point location) {
		this.location = location;
	}

	@Override
	public void setNest(IEntity<?, ?> entity) {
		this.nest = entity;
	}
	
	public AValue getValueForAttribute(String property){
		Optional<ASurveyAttribute> opAtt = attributes.keySet()
				.stream().filter(att -> att.getAttributeName().equals(property)).findFirst();
		if(opAtt.isPresent())
			return attributes.get(opAtt.get());
		throw new NullPointerException("Attribute "+property+" does not exist in "+this.getClass().getSimpleName());
	}

	

}
