package core.io.geo.entity;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.geotools.geometry.Envelope2D;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;

import core.io.geo.entity.attribute.AGeoAttribute;
import core.io.geo.entity.attribute.RawGeoAttribute;
import core.io.geo.entity.attribute.value.AGeoValue;
import core.io.geo.entity.attribute.value.RawGeoData;

public class GeoEntityFactory {

	private Set<AGeoAttribute> attributes;

	private static String ATTRIBUTE_PIXEL_BAND = "Band";

	public GeoEntityFactory(Set<AGeoAttribute> attributes) {
		this.attributes = attributes;
	}

	public GSFeature createGeoEntity(Feature feature) {
		Set<AGeoValue> values = new HashSet<>();
		for(Property property : feature.getProperties()){
			Optional<AGeoAttribute> opAtt = attributes
					.stream().filter(att -> att.getAttributeName().equals(property.getName().getLocalPart()))
					.findFirst();
			AGeoAttribute attribute = opAtt.isPresent() ? opAtt.get() : new RawGeoAttribute(property.getName().getLocalPart());
			if(!opAtt.isPresent())
				attributes.add(attribute);
			Optional<AGeoValue> opVal = attribute.getValues()
					.stream().filter(val -> val.getInputStringValue().equals(property.getValue().toString()))
					.findFirst();
			AGeoValue value = opVal.isPresent() ? opVal.get() : new RawGeoData(attribute, property.getValue());
			if(!opVal.isPresent())
				attribute.addValue(value);
			values.add(value);
		}
		return new GSFeature(values, feature);
	}

	public GSPixel createGeoEntity(Number[] pixelBands, Envelope2D pixel, int gridX, int gridY) {
		Set<AGeoValue> values = new HashSet<>();
		for(int i = 0; i < pixelBands.length; i++){
			String bandsName = ATTRIBUTE_PIXEL_BAND+"_"+i;
			Optional<AGeoAttribute> opAtt = attributes
					.stream().filter(att -> att.getAttributeName().equals(bandsName))
					.findFirst();
			AGeoAttribute attribute = opAtt.isPresent() ? opAtt.get() : new RawGeoAttribute(bandsName);
			if(!opAtt.isPresent())
				attributes.add(attribute);
			int idx = i;
			Optional<AGeoValue> opVal = values
					.stream().filter(val -> val.getInputStringValue().equals(pixelBands[idx].toString()))
					.findFirst();
			AGeoValue value = opVal.isPresent() ? opVal.get() : new RawGeoData(attribute, pixelBands[i]);
			if(!opVal.isPresent())
				attribute.addValue(value);
			values.add(value);
		}
		return new GSPixel(values, pixel, gridX, gridY);
	}

}
