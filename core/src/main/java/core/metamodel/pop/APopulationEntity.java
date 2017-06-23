package core.metamodel.pop;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import core.metamodel.IEntity;
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
	
	public abstract APopulationEntity clone();
	

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
