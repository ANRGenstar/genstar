package gospl.metamodel;

import java.util.Map;

import com.vividsolutions.jts.geom.Point;

import core.io.geo.entity.AGeoEntity;
import core.io.survey.entity.AGenstarEntity;
import core.io.survey.entity.attribut.AGenstarAttribute;
import core.io.survey.entity.attribut.value.AGenstarValue;

public class GosplEntity extends AGenstarEntity {

	private Point location = null;
	private AGeoEntity nest = null;

	public GosplEntity(Map<AGenstarAttribute, AGenstarValue> attributes){
		super(attributes);
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
