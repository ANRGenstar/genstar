package spll.entity;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.geotools.feature.type.BasicFeatureTypes;
import org.geotools.geometry.Envelope2D;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;

import core.metamodel.geo.AGeoAttribute;
import core.metamodel.geo.AGeoValue;
import spll.entity.attribute.RawGeoAttribute;
import spll.entity.attribute.value.RawGeoData;

public class GeoEntityFactory {

	private Set<AGeoAttribute> attributes;

	private static String ATTRIBUTE_PIXEL_BAND = "Band";

	public GeoEntityFactory(Set<AGeoAttribute> attributes) {
		this.attributes = attributes;
	}

	/**
	 * WARNING: could lead to concurrent access exception in case of parallel call
	 * 
	 * @param feature
	 * @return
	 */
	public GSFeature createGeoEntity(Feature feature, List<String> atts) {
		Set<AGeoValue> values = new HashSet<>();
		for(Property property : feature.getProperties()){
			String name = property.getName().getLocalPart();
			if ( BasicFeatureTypes.GEOMETRY_ATTRIBUTE_NAME.equals(name) || ((atts != null) && !atts.contains(name))) continue;
			Optional<AGeoAttribute> opAtt = attributes
					.stream().filter(att -> att.getAttributeName().equals(name))
					.findFirst();
			AGeoAttribute attribute = opAtt.isPresent() ? opAtt.get() : new RawGeoAttribute(name);
			if(!opAtt.isPresent())
				attributes.add(attribute);
			Optional<AGeoValue> opVal = attribute.getValues()
					.stream().filter(val -> val.getInputStringValue().equals(name))
					.findFirst();
			AGeoValue value = opVal.isPresent() ? opVal.get() : new RawGeoData(attribute, property.getValue());
			if(!opVal.isPresent())
				attribute.addValue(value);
			values.add(value);
		}
		return new GSFeature(values, feature);
	}

	/**
	 * WARNING: could lead to concurrent access exception in case of parallel call
	 * 
	 * @param pixelBands
	 * @param pixel
	 * @param gridX
	 * @param gridY
	 * @return
	 */
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
