package core.io.survey.entity;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import com.vividsolutions.jts.geom.Point;

import core.io.geo.entity.AGeoEntity;
import core.io.survey.entity.attribut.ASurveyAttribute;
import core.io.survey.entity.attribut.value.ASurveyValue;
import core.metamodel.IEntity;

public abstract class ASurveyEntity implements IEntity<ASurveyAttribute, ASurveyValue> {

	private Map<ASurveyAttribute, ASurveyValue> attributes;
	
	public ASurveyEntity(Map<ASurveyAttribute, ASurveyValue> attributes) {
		this.attributes = attributes;
	}

	@Override
	public Collection<ASurveyAttribute> getAttributes() {
		return attributes.keySet();
	}
	
	@Override
	public Collection<ASurveyValue> getValues() {
		return Collections.unmodifiableCollection(attributes.values());
	}

	@Override
	public ASurveyValue getValueForAttribute(ASurveyAttribute attribute) {
		return attributes.get(attribute);
	}
	
	@Override
	public ASurveyValue getValueForAttribute(String property){
		Optional<ASurveyAttribute> opAtt = attributes.keySet()
				.stream().filter(att -> att.getAttributeName().equals(property)).findFirst();
		if(opAtt.isPresent())
			return attributes.get(opAtt.get());
		throw new NullPointerException("Attribute "+property+" does not exist in "+this.getClass().getSimpleName());
	}

	/**
	 * Retrieve the localtion of the agent as a point
	 * 
	 * @return a point of type {@link Point}
	 */
	public abstract Point getLocation();

	/**
	 * Retrieve the most significant enclosing geographical entity this
	 * entity is situated. It represents 'home's entity 
	 * 
	 * @return
	 */
	public abstract AGeoEntity getNest();

	/**
	 * Change the location of the entity
	 * 
	 * @param location
	 */
	public abstract void setLocation(Point location);

	/**
	 * Change the nest of the entity
	 * 
	 * @param entity
	 */
	public abstract void setNest(AGeoEntity entity);

}
