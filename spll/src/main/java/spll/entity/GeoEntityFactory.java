package spll.entity;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import core.metamodel.value.geo.IValue;
import spll.entity.attribute.RawGeoAttribute;
import spll.entity.attribute.value.RawGeoData;

/**
 * The factory to safely create Spll geographical entity
 * <p>
 *  TODO: revise what IValue should be
 * 
 * @author kevinchapuis
 *
 */
public class GeoEntityFactory {

	public static String ATTRIBUTE_PIXEL_BAND = "Band";
	public static String ATTRIBUTE_FEATURE_POP = "Population";
	
	private Map<String, AGeoAttribute> attributes;
	
	private SimpleFeatureBuilder contingencyFeatureBuilder;
	
	private Logger log = LogManager.getLogger();

	/**
	 * Defines the set of attributes for entities to be created. This set will 
	 * be the support to add new value and recall them to avoid duplicates
	 * 
	 * @param attributes
	 */
	public GeoEntityFactory(Set<AGeoAttribute> attributes) {
		this.attributes = new ConcurrentHashMap<String, AGeoAttribute>();
		for (AGeoAttribute att: attributes) {
			this.attributes.put(att.getAttributeName(), att);
		}
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
        builder.setName("SimpleFeatureTypeBuilder");
        builder.setCRS(schema.getCoordinateReferenceSystem()); // <- Coordinate reference system
        builder.add(BasicFeatureTypes.GEOMETRY_ATTRIBUTE_NAME, schema.getGeometryDescriptor().getType().getBinding());
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
	public SpllFeature createGeoEntity(Feature feature, List<String> attList) {
		Set<IValue> values = new HashSet<>();
		NumberFormat defaultFormat = NumberFormat.getInstance();
		for(Property property : feature.getProperties()){
			String name = property.getName().getLocalPart();
			if ( BasicFeatureTypes.GEOMETRY_ATTRIBUTE_NAME.equals(name) || (!attList.isEmpty() && !attList.contains(name))) continue;
			AGeoAttribute attribute = attributes.get(name);
			if (attribute == null) {
				attribute = new RawGeoAttribute(name);
				attributes.put(name,attribute);
			}
			IValue value = attribute.addValue( property.getValue().toString());
			if (value == null) {
				try {
					Number val = defaultFormat.parse(property.getValue().toString());
					value = new RawGeoData(attribute, val);
				} catch (ParseException e){
					value = new RawGeoData(attribute, property.getValue());
				}
				attribute.addValue(value);
			}
			values.add(value);
		}
		return new SpllFeature(values, feature);
	}
	
	/**
	 * 
	 * 
	 * @param the_geom
	 * @param featureValues
	 * @return
	 */
	public SpllFeature createGeoEntity(Geometry the_geom, Set<IValue> featureValues){
		// Use factory defined feature constructor to build the inner feature
		contingencyFeatureBuilder.add(the_geom);
		featureValues.stream().forEach(values -> 
			contingencyFeatureBuilder.set(values.getAttribute().getAttributeName(), 
					values.isNumericalValue() ? values.getNumericalValue() : values.getInputStringValue()));
		Feature feat = contingencyFeatureBuilder.buildFeature(null);
		// Add non previously encountered attribute to attributes set
		
		for (IValue val: featureValues) {
			if (!attributes.containsKey(val.getInputStringValue())) 
				attributes.put(val.getInputStringValue(), val.getAttribute());
		}
		
		// Return created GSFeature
		return new SpllFeature(featureValues, feat);
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
	public SpllPixel createGeoEntity(Number[] pixelBands, Envelope2D pixel, int gridX, int gridY) {
		Set<IValue> values = new HashSet<>();
		for(int i = 0; i < pixelBands.length; i++){
			String bandsName = ATTRIBUTE_PIXEL_BAND+i;
			AGeoAttribute attribute = attributes.get(bandsName);
			if (attribute == null) {
				attribute = new RawGeoAttribute(bandsName);
				attributes.put(bandsName,attribute);
			}
				
			int idx = i;
			Optional<IValue> opVal = values
					.stream().filter(val -> val.getInputStringValue().equals(pixelBands[idx].toString()))
					.findFirst();
			IValue value = opVal.isPresent() ? opVal.get() : new RawGeoData(attribute, pixelBands[i]);
			if(!opVal.isPresent())
				attribute.addValue(value);
			values.add(value);
		}
		return new SpllPixel(values, pixel, gridX, gridY);
	}

}
