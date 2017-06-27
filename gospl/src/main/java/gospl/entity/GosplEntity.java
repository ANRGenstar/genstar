package gospl.entity;

import java.util.Map;

import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;

/**
 * A GoSPL Entity is a population entity with a geolocation
 * 
 *
 */
public class GosplEntity extends APopulationEntity {

	public GosplEntity(Map<APopulationAttribute, APopulationValue> attributes){
		super(attributes);
	}

	public GosplEntity(){
		super();
	}
	
	@Override
	public GosplEntity clone(){
		return new GosplEntity(this.getAttributesMap());
	}

}
