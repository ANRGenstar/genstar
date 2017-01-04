package spll.entity;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.feature.type.BasicFeatureTypes;
import org.geotools.geometry.Envelope2D;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Geometry;

import core.metamodel.geo.AGeoAttribute;
import core.metamodel.geo.AGeoValue;
import spll.entity.attribute.RawGeoAttribute;
import spll.entity.attribute.value.RawGeoData;

/**
 * The factory to safely create Spll geographical entity
 * 
 *  TODO: revise what AGeoValue should be
 * 
 * @author kevinchapuis
 *
 */
public class GeoEntityFactory {

	public static String ATTRIBUTE_PIXEL_BAND = "Band";
	
	private Set<AGeoAttribute> attributes;
	
	private SimpleFeatureBuilder contingencyFeatureBuilder;
	
	private Logger log = LogManager.getLogger();

	/**
	 * Defines the set of attributes for entities to be created. This set will 
	 * be the support to add new value and recall them to avoid duplicates
	 * 
	 * @param attributes
	 */
	public GeoEntityFactory(Set<AGeoAttribute> attributes) {
		this.attributes = ConcurrentHashMap.newKeySet();
		this.attributes.addAll(attributes);
	}
	
	/**
	 * In addition to set of attribute, also defines a way to create Geotools
	 * SimpleFeature to facilitate the creation of vector style geo entity
	 * 
	 * @param attributes
	 * @param featureTypeName
	 * @param crs
	 * @param geomClazz
	 */
	public GeoEntityFactory(Set<AGeoAttribute> attributes, SimpleFeatureType schema){
		this(attributes);
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName("contingency");
        builder.setCRS(schema.getCoordinateReferenceSystem()); // <- Coordinate reference system

        /*
         *  WARNING: after all not sure what getBinding really return
         */
        builder.add("the_geom", schema.getGeometryDescriptor().getType().getBinding());
        log.debug("Setup a builder ({}) with {} geometry and [{}] attribute list",
        		builder.getName(),
        		schema.getGeometryDescriptor().getType().getBinding().getSimpleName(), 
        		attributes.stream().map(a -> a.getAttributeName()).collect(Collectors.joining(", ")));
        for(AGeoAttribute attribute : attributes)
            builder.add(attribute.getAttributeName(), attribute.getValues().stream()
        			.allMatch(value -> value.isNumericalValue()) ? Number.class : String.class);
        
        this.contingencyFeatureBuilder = new SimpleFeatureBuilder(builder.buildFeatureType());
	}
	
	// ---------------------------------------------------------- //

	/**
	 * Create a vector style entity
	 * 
	 * @param feature
	 * @return
	 */
	public GSFeature createGeoEntity(Feature feature, List<String> attList) {
		Set<AGeoValue> values = new HashSet<>();
		for(Property property : feature.getProperties()){
			String name = property.getName().getLocalPart();
			if ( BasicFeatureTypes.GEOMETRY_ATTRIBUTE_NAME.equals(name) || (attList != null && !attList.contains(name))) continue;
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
	 * Create a raster style entity
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
			String bandsName = ATTRIBUTE_PIXEL_BAND+i;
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
	
	// ----------------------- GEOTOOLS RELATED FEATURE CREATION ----------------------- //
	
	/**
	 * 
	 * 
	 * @param the_geom
	 * @param featureValues
	 * @return
	 */
	public Feature createContingencyFeature(Geometry the_geom, Set<AGeoValue> featureValues){
		contingencyFeatureBuilder.add(the_geom);
		featureValues.stream().forEach(values -> 
			contingencyFeatureBuilder.set(values.getAttribute().getAttributeName(), 
					values.isNumericalValue() ? values.getNumericalValue() : values.getInputStringValue()));
		return contingencyFeatureBuilder.buildFeature(null);
	}

}
