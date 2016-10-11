package io.geofile.data;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.geotools.data.DataUtilities;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.opengis.feature.Feature;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.IllegalAttributeException;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.identity.FeatureId;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;

public class GSFeature implements IGeoGSAttribute, Feature {

	private final Feature innerFeature;
	
	public GSFeature(Feature innerFeature) {
		this.innerFeature = innerFeature;
	}
	
	@Override
	public Geometry getPosition() {
		return (Geometry) innerFeature.getDefaultGeometryProperty().getValue();
	}
	
	@Override
	public GSFeature transposeToGenstarFeature() {
		return this;
	}
	
	@Override
	public GSFeature transposeToGenstarFeature(CoordinateReferenceSystem crs) {
		SimpleFeatureType schema = null;
		try {
			schema = DataUtilities.createSubType((SimpleFeatureType) innerFeature, null, crs);
		} catch (SchemaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SimpleFeatureBuilder builder = new SimpleFeatureBuilder(schema);
		builder.init((SimpleFeature) innerFeature);
		return new GSFeature(builder.buildFeature(innerFeature.getIdentifier().getID()));
	}
	
	@Override
	public IGeoData getValue(String property) {
		return new GSGeoData(this.getProperty(property).getValue());
	}
	
	@Override
	public boolean isNoDataValue(String property) {
		if(this.getProperty(property).getValue() == null)
			return true;
		return false;
	}
	
	@Override
	public Collection<String> getPropertiesAttribute() {
		return innerFeature.getProperties().stream().map(p -> p.getName().toString()).collect(Collectors.toList());
	}

	
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
	public String getGenstarName() {
		return innerFeature.getName().getLocalPart();
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
