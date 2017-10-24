package gospl;

import java.util.HashMap;
import java.util.Map;

import core.metamodel.pop.ADemoEntity;
import core.metamodel.pop.attribute.DemographicAttribute;
import core.metamodel.value.IValue;

/**
 * A GoSPL Entity is a population entity
 * 
 *
 */
public class GosplEntity extends ADemoEntity {
	
	public GosplEntity(Map<DemographicAttribute<? extends IValue>, IValue> attributes){
		super(attributes);
	}

	public GosplEntity(){
		super();
	}
	
	@Override
	public GosplEntity clone(){
		return new GosplEntity(new HashMap<>(this.getAttributeMap()));
	}

}
