package core.io.geo.entity;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.opengis.feature.Feature;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.IllegalAttributeException;
import org.opengis.feature.Property;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.identity.FeatureId;
import org.opengis.geometry.BoundingBox;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import core.io.geo.entity.attribute.value.AGeoValue;

public class GSFeature extends AGeoEntity implements Feature {

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
	
	////////////////////////////////////////////////////////////
	// --------------- Feature implementation --------------- //
	////////////////////////////////////////////////////////////
	
	@Override
	public Collection<? extends Property> getValue() {
		return innerFeature.getValue();
	}
	
	@Override
	public void setValue(Collection<Property> values) {
		innerFeature.setValue(values);
	}

	@Override
	public Collection<Property> getProperties(Name name) {
		return innerFeature.getProperties(name);
	}

	@Override
	public Property getProperty(Name name) {
		return innerFeature.getProperty(name);
	}

	@Override
	public Collection<Property> getProperties(String name) {
		return innerFeature.getProperties(name);
	}

	@Override
	public Collection<Property> getProperties() {
		return innerFeature.getProperties();
	}
	
	@Override
	public Property getProperty(String name) {
		return innerFeature.getProperty(name);
	}

	@Override
	public void validate() throws IllegalAttributeException {
		innerFeature.validate();
	}

	@Override
	public AttributeDescriptor getDescriptor() {
		return innerFeature.getDescriptor();
	}

	@Override
	public void setValue(Object newValue) {
		innerFeature.setValue(newValue);
	}
	
	@Override
	public Name getName() {
		return innerFeature.getName();
	}

	@Override
	public boolean isNillable() {
		return innerFeature.isNillable();
	}

	@Override
	public Map<Object, Object> getUserData() {
		return innerFeature.getUserData();
	}

	@Override
	public FeatureType getType() {
		return innerFeature.getType();
	}

	@Override
	public FeatureId getIdentifier() {
		return innerFeature.getIdentifier();
	}

	@Override
	public BoundingBox getBounds() {
		return innerFeature.getBounds();
	}

	@Override
	public GeometryAttribute getDefaultGeometryProperty() {
		return innerFeature.getDefaultGeometryProperty();
	}

	@Override
	public void setDefaultGeometryProperty(GeometryAttribute geometryAttribute) {
		innerFeature.setDefaultGeometryProperty(geometryAttribute);
	}
	
	@Override
	public String toString() {
		return "Feature of type: "+innerFeature.getType()+" with default geometry being "+this.getDefaultGeometryProperty().getType();
	}

}
