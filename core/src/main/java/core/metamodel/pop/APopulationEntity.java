package core.metamodel.pop;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import com.vividsolutions.jts.geom.Point;

import core.metamodel.IEntity;
import core.metamodel.geo.AGeoEntity;

public abstract class APopulationEntity implements IEntity<APopulationAttribute, APopulationValue> {

	private Map<APopulationAttribute, APopulationValue> attributes;
	
	public APopulationEntity(Map<APopulationAttribute, APopulationValue> attributes) {
		this.attributes = attributes;
	}

	@Override
	public Collection<APopulationAttribute> getAttributes() {
		return attributes.keySet();
	}
	
	@Override
	public Collection<APopulationValue> getValues() {
		return Collections.unmodifiableCollection(attributes.values());
	}

	@Override
	public APopulationValue getValueForAttribute(APopulationAttribute attribute) {
		return attributes.get(attribute);
	}
	
	@Override
	public APopulationValue getValueForAttribute(String property){
		Optional<APopulationAttribute> opAtt = attributes.keySet()
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
