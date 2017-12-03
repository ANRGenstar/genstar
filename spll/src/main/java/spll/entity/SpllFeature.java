package spll.entity;

import java.util.Map;

import org.opengis.feature.Feature;

import com.vividsolutions.jts.geom.Geometry;

import core.metamodel.attribute.geographic.GeographicAttribute;
import core.metamodel.entity.AGeoEntity;
import core.metamodel.value.IValue;

public class SpllFeature extends AGeoEntity<IValue> {
 
	private final Feature innerFeature;
	
	protected SpllFeature(Map<GeographicAttribute<? extends IValue>, IValue> values, Feature innerFeature) {
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
