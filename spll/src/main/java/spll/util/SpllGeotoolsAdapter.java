package spll.util;

import java.util.Set;

import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.feature.type.BasicFeatureTypes;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import core.metamodel.attribute.geographic.GeographicAttribute;
import core.metamodel.attribute.geographic.GeographicAttributeFactory;
import core.metamodel.value.IValue;
import core.util.data.GSEnumDataType;

/**
 * A simple utility class that unable 
 * 
 * @author kevinchapuis
 *
 */
public class SpllGeotoolsAdapter {

	private static SpllGeotoolsAdapter gaw = new SpllGeotoolsAdapter();
	
	private SpllGeotoolsAdapter() {};
	
	public static SpllGeotoolsAdapter getInstance() {
		return gaw;
	}
	
	/**
	 * Establish a Geotools feature type from a set of Genstar attribute 
	 * <p>
	 * From a set of {@link GeographicAttribute} to a {@link SimpleFeatureType}
	 * 
	 * @param attributes
	 * @return
	 */
	public SimpleFeatureType getGeotoolsFeatureType(String name,
			Set<GeographicAttribute<? extends IValue>> attributes, CoordinateReferenceSystem crs,
			GeometryDescriptor geometry) {
		SimpleFeatureTypeBuilder stb = new SimpleFeatureTypeBuilder();
		stb.setName(name);
		stb.setCRS(crs);
		stb.add(BasicFeatureTypes.GEOMETRY_ATTRIBUTE_NAME, geometry.getType().getBinding());
		attributes.stream().forEach(att -> stb.add(att.getAttributeName(), IValue.class));
		return stb.buildFeatureType();
	}
	
	/**
	 * Transpose a Geotools property into a Genstar attribute
	 * <p>
	 * From {@link Property} to {@link GeographicAttribute} with basic value transposition that can be of type
	 * {@link GSEnumDataType#Boolean} {@link GSEnumDataType#Continue} {@link GSEnumDataType#Integer} {@link GSEnumDataType#Nominal}
	 * 
	 * @param property
	 * @return
	 */
	public GeographicAttribute<? extends IValue> getGeographicAttribute(Property property) {
		
		return GeographicAttributeFactory.getFactory().createAttribute(
					property.getName().getLocalPart(), 
					GSEnumDataType.getTypeForJavaType(property.getType().getBinding())
					);
		/*
		return 
				Stream.of(GSEnumDataType.Boolean, GSEnumDataType.Continue, GSEnumDataType.Integer, GSEnumDataType.Nominal)
				.filter(gsType -> gsType.getInnerType().equals(property.getType().getBinding())).findAny().get());
				*/
	}
	
}
