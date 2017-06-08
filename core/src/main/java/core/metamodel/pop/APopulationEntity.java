package core.metamodel.pop;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import com.vividsolutions.jts.geom.Point;

import core.metamodel.IEntity;
import core.metamodel.geo.AGeoEntity;
import core.util.GSUniqueIDGenerator;

/**
 * TODO: javadoc
 * 
 * @author kevinchapuis
 *
 */
public abstract class APopulationEntity implements IEntity<APopulationAttribute, APopulationValue> {

	private Map<APopulationAttribute, APopulationValue> attributes;
	private int uniqueID;
	
	public APopulationEntity(Map<APopulationAttribute, APopulationValue> attributes) {
		this.attributes = attributes;
		this.uniqueID = GSUniqueIDGenerator.getInstance().getNextID();
	}
	
	/**
	 * Access restricted to the inner collection that stores attribute / value pairs
	 * @return
	 */
	protected Map<APopulationAttribute, APopulationValue> getAttributesMap(){
		return attributes;
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
	 * Returns a shadow clones, i.e. the same entity expect it must
	 * returns null to equals request:
	 * <p>
	 * {@code this.clone().equals(this)} must returns false
	 * 
	 * @return
	 */
	public abstract APopulationEntity clone();

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

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((attributes == null) ? 0 : attributes.hashCode());
		result = prime * result + uniqueID;
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		APopulationEntity other = (APopulationEntity) obj;
		if (attributes == null) {
			if (other.attributes != null)
				return false;
		} else if (!attributes.equals(other.attributes))
			return false;
		if (uniqueID != other.uniqueID)
			return false;
		return true;
	}

}
