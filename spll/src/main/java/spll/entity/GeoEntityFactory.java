package spll.entity;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import core.metamodel.geo.AGeoValue;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationValue;
import core.util.data.GSEnumDataType;
import gospl.GosplPopulation;
import gospl.entity.GosplEntity;
import gospl.entity.attribute.value.UniqueValue;
import spll.entity.attribute.RawGeoAttribute;
import spll.entity.attribute.value.RawGeoData;
import spll.io.SPLVectorFile;

/**
 * The factory to safely create Spll geographical entity
 * <p>
 *  TODO: revise what AGeoValue should be
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
		Set<AGeoValue> values = new HashSet<>();
		NumberFormat defaultFormat = NumberFormat.getInstance();
		for(Property property : feature.getProperties()){
			String name = property.getName().getLocalPart();
			if ( BasicFeatureTypes.GEOMETRY_ATTRIBUTE_NAME.equals(name) || (!attList.isEmpty() && !attList.contains(name))) continue;
			AGeoAttribute attribute = attributes.get(name);
			if (attribute == null) {
				attribute = new RawGeoAttribute(name);
				attributes.put(name,attribute);
			}
			AGeoValue value = attribute.getValue( property.getValue().toString());
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
	public SpllFeature createGeoEntity(Geometry the_geom, Set<AGeoValue> featureValues){
		// Use factory defined feature constructor to build the inner feature
		contingencyFeatureBuilder.add(the_geom);
		featureValues.stream().forEach(values -> 
			contingencyFeatureBuilder.set(values.getAttribute().getAttributeName(), 
					values.isNumericalValue() ? values.getNumericalValue() : values.getInputStringValue()));
		Feature feat = contingencyFeatureBuilder.buildFeature(null);
		// Add non previously encountered attribute to attributes set
		
		for (AGeoValue val: featureValues) {
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
		Set<AGeoValue> values = new HashSet<>();
		for(int i = 0; i < pixelBands.length; i++){
			String bandsName = ATTRIBUTE_PIXEL_BAND+i;
			AGeoAttribute attribute = attributes.get(bandsName);
			if (attribute == null) {
				attribute = new RawGeoAttribute(bandsName);
				attributes.put(bandsName,attribute);
			}
				
			int idx = i;
			Optional<AGeoValue> opVal = values
					.stream().filter(val -> val.getInputStringValue().equals(pixelBands[idx].toString()))
					.findFirst();
			AGeoValue value = opVal.isPresent() ? opVal.get() : new RawGeoData(attribute, pixelBands[i]);
			if(!opVal.isPresent())
				attribute.addValue(value);
			values.add(value);
		}
		return new SpllPixel(values, pixel, gridX, gridY);
	}

	/**
	 * Creates a GoSPL population from a shapefile. 
	 * It browses the different Features from the shapefile (which correspond to entities),
	 * parses their various attributes and keeps only the attributes declared in the 
	 * dictionnary of data. 
	 * Returns these entities as a GoSPL population, without the corresponding geometry.
	 * 
	 * @param sfAdmin a shapefile
	 * @param dictionnary a list of the attributes to read 
	 * @param maxentities the maximum size of entities to read
	 * @return
	 */
	public static GosplPopulation createGosplPopulationFromShapefile(SPLVectorFile sfAdmin, Collection<APopulationAttribute> dictionnary, int maxentities) {
		
		// create an index of the dictionnary on names
		Map<String,APopulationAttribute> dictionnaryName2attribute = new HashMap<>(dictionnary.size());
		System.out.print("working on attributes: "+dictionnaryName2attribute);
		for (APopulationAttribute a: dictionnary)
			dictionnaryName2attribute.put(a.getAttributeName(), a);

		// the resulting population
		GosplPopulation pop = new GosplPopulation();
		
		// just for information
		System.out.println("read the shapefile.");
		System.out.println("\nhere are the attributes available in the shapefile:");
		for (AGeoAttribute att: sfAdmin.getGeoAttributes()) {
			System.out.println(att.getAttributeName()+": "+att.getValues().size()); // att.getValuesAsString()
		}
		
		// will contain the list of all the attributes which were ignored 
		Set<String> ignoredAttributes = new HashSet<>();
		
		// iterate all the geo entities
		Iterator<SpllFeature> itGeoEntity = sfAdmin.getGeoEntityIterator();
		int i=0;
		while (itGeoEntity.hasNext()) {
			SpllFeature feature = itGeoEntity.next();
			System.out.println("\nfound SpellFeature: "+feature.getGenstarName());
			Map<APopulationAttribute,APopulationValue> attribute2value = new HashMap<>(dictionnary.size());
			
			for (AGeoValue v : feature.getValues()) {
				System.out.println(v.getAttribute().getAttributeName()+ "="+v.getInputStringValue());
				APopulationAttribute gosplType = dictionnaryName2attribute.get(v.getAttribute().getAttributeName());
				if (gosplType == null) {
					System.err.println("attribute not defined in dict: "+v.getAttribute().getAttributeName());
					ignoredAttributes.add(v.getAttribute().getAttributeName());
					continue;
				}
				try {
					APopulationValue value = gosplType.getValue(v.getInputStringValue());
					if (value == null)
						value = new UniqueValue(v.getInputStringValue(), GSEnumDataType.String, gosplType);
					attribute2value.put(gosplType, value);

				} catch (NullPointerException e) {
					ignoredAttributes.add(v.getAttribute().getAttributeName());
				}
				
			}
			GosplEntity entity = new GosplEntity(attribute2value);
			pop.add(entity);
			if (i++ >= maxentities)
				break;
		}
		
		if (!ignoredAttributes.isEmpty()) {
			System.err.println("ignored attributes not defined in the dictionnary: "+ignoredAttributes);
		}
		
		return pop;
		
	}
}
