package core.io.geo.entity;

import java.util.Set;

import org.opengis.feature.Feature;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import core.io.geo.entity.attribute.value.AGeoValue;

public class GSFeature extends AGeoEntity {

	private final Feature innerFeature;
	
	protected GSFeature(Set<AGeoValue> values, Feature innerFeature) {
		super(values, innerFeature.getIdentifier().getID());
		this.innerFeature = innerFeature;
	}
	
	public Feature getInnerFeature(){
		return innerFeature;
	}
	
	@Override
	public double getArea() {
		return this.getGeometry().getArea();
	}
	
	@Override
	public Point getPosition() {
		return this.getGeometry().getCentroid();
	}
	
	@Override
	public Geometry getGeometry() {
		return (Geometry) innerFeature.getDefaultGeometryProperty().getValue();
	}
	
	@Override
	public String getGenstarName(){
		return innerFeature.getIdentifier().getID();
	}
	
	@Override
	public String toString() {
		return "Feature of type: "+innerFeature.getType()+" with default geometry being "+this.getGeometry().getGeometryType();
	}

}
