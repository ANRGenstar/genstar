package spll.entity;

import java.util.Set;

import org.opengis.feature.Feature;

import com.vividsolutions.jts.geom.Geometry;

import core.metamodel.geo.AGeoEntity;
import core.metamodel.geo.AGeoValue;

public class SpllFeature extends AGeoEntity {
 
	private final Feature innerFeature;
	
	protected SpllFeature(Set<AGeoValue> values, Feature innerFeature) {
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
