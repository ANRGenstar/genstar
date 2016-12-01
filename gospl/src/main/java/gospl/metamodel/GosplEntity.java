package gospl.metamodel;

import java.util.Map;

import com.vividsolutions.jts.geom.Point;

import core.io.geo.entity.AGeoEntity;
import core.io.survey.entity.ASurveyEntity;
import core.io.survey.entity.attribut.ASurveyAttribute;
import core.io.survey.entity.attribut.value.ASurveyValue;

public class GosplEntity extends ASurveyEntity {

	private Point location = null;
	private AGeoEntity nest = null;

	public GosplEntity(Map<ASurveyAttribute, ASurveyValue> attributes){
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
