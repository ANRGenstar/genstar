package gospl.entity;

import java.util.Map;

import com.vividsolutions.jts.geom.Point;

import core.metamodel.geo.AGeoEntity;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;

/**
 * A GoSPL Entity is a population entity with a geolocation
 * 
 *
 */
public class GosplEntity extends APopulationEntity {

	private Point location = null;
	private AGeoEntity nest = null;

	public GosplEntity(Map<APopulationAttribute, APopulationValue> attributes){
		super(attributes);
	}

	public GosplEntity(){
		super();
	}
	
	@Override
	public Point getLocation() {
		return location;
	}

	@Override
	public AGeoEntity getNest() {
		return nest;
	}

	@Override
	public void setLocation(Point location) {
		this.location = location;
	}

	@Override
	public void setNest(AGeoEntity entity) {
		this.nest = entity;
	}


	

}
