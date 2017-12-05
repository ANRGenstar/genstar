package spll;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import core.metamodel.IPopulation;
import core.metamodel.attribute.demographic.DemographicAttribute;
import core.metamodel.attribute.geographic.GeographicAttribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.entity.AGeoEntity;
import core.metamodel.io.IGSGeofile;
import core.metamodel.value.IValue;
import gospl.GosplEntity;
import spll.entity.SpllFeature;
import spll.io.SPLGeofileBuilder;
import spll.io.SPLVectorFile;
import spll.io.exception.InvalidGeoFormatException;
import spll.util.SpllUtil;

/**
 * A population of spatialized entities.
 * 
 * @author Kevin Chapuis
 * @author Samuel Thiriot
 */
public class SpllPopulation implements IPopulation<SpllEntity, DemographicAttribute<? extends IValue>> {

	private Set<SpllEntity> population;
	private IGSGeofile<? extends AGeoEntity<? extends IValue>, IValue> geoFile; 

	private Set<DemographicAttribute<? extends IValue>> attributes;
	
	public SpllPopulation(IPopulation<ADemoEntity, DemographicAttribute<? extends IValue>> population,
			IGSGeofile<? extends AGeoEntity<? extends IValue>, IValue> geoFile) {
		this.population = population.stream().map(entity -> new SpllEntity(entity))
				.collect(Collectors.toSet());
		this.attributes = population.getPopulationAttributes();
		this.geoFile = geoFile;
	}

	/**
	 * Creates a SPLL population from a file which will be decoded based on the dictionnary passed as a parameter.
	 * 
	 * @param sfFile
	 * @param dictionnary
	 * @param charset (or null for default)
	 * @param maxEntities
	 * @throws IOException
	 * @throws InvalidGeoFormatException
	 */
	public SpllPopulation(	File sfFile, 
							Collection<DemographicAttribute<? extends IValue>> dictionnary, 
							Charset charset, 
							int maxEntities) throws IOException, InvalidGeoFormatException {

		this.population=new HashSet<>();
		this.attributes=new HashSet<>();
		
		System.err.println("dict at spll :"+dictionnary);
		System.err.println("max entities:"+maxEntities);

		List<String> attributesNamesToKeep = dictionnary.stream()
													.map(DemographicAttribute::getAttributeName)
													.collect(Collectors.toList());
		SPLVectorFile sf = SPLGeofileBuilder.getShapeFile(sfFile,  attributesNamesToKeep, charset);
		
		addDataFromVector(sf, dictionnary, maxEntities);
	}
	
	/**
	 * Creates a SPLL population from a shapefile based on the dictionnary passed as parameter. 
	 * Attributes which are not explicitely added are ignored.
	 * @param sf
	 * @param dictionnary
	 * @param the maximum count of entities to read (-1 to ignore)
	 */
	public SpllPopulation(	SPLVectorFile sf, 
							Collection<DemographicAttribute<? extends IValue>> dictionnary, 
							int maxEntities) {
		
		this.population=new HashSet<>();
		this.attributes=new HashSet<>();
		
		System.err.println("max entities:"+maxEntities);

		addDataFromVector(sf, dictionnary, maxEntities);	
	}
	
	/**
	 * Adds 
	 * @param sf
	 * @param dictionnary
	 * @param maxEntities
	 */
	private void addDataFromVector(	SPLVectorFile sf, 
									Collection<DemographicAttribute<? extends IValue>> dictionnary, 
									int maxEntities) {

		// index the dictionnary by name
		Map<String,DemographicAttribute<? extends IValue>> dictionnaryName2attribute = new HashMap<>(dictionnary.size());
		for (DemographicAttribute<? extends IValue> a: dictionnary)
			dictionnaryName2attribute.put(a.getAttributeName(), a);
		//System.out.println("working on attributes: "+dictionnaryName2attribute);

		// will contain the list of all the attributes which were ignored 
		Set<String> ignoredAttributes = new HashSet<>();
		//Map<String,Set<String>> attributeName2ignoredValues = new HashMap<>();
		
		// iterate entities
		Iterator<SpllFeature> itGeoEntity = sf.getGeoEntityIterator();
		int i=0;
		while (itGeoEntity.hasNext()) {
			
			// retrieve the geospatial entity considered
			SpllFeature feature = itGeoEntity.next();
			//System.out.println("working on feature: "+feature.getGenstarName());

			Map<DemographicAttribute<? extends IValue>,IValue> attribute2value = new HashMap<>(dictionnary.size());
			
			for (Map.Entry<GeographicAttribute<? extends IValue>, IValue> attributeAndValue : 
								feature.getAttributeMap().entrySet()) {
				
				final GeographicAttribute<? extends IValue> attribute = attributeAndValue.getKey();
				final IValue value = attributeAndValue.getValue();
				

				// find in the dictionnary the attribute definition with the same name as this geo attribute
				DemographicAttribute<? extends IValue> gosplType = dictionnaryName2attribute.get(
						attribute.getAttributeName());
				
				// skip attributes not defined
				if (gosplType == null) {
					ignoredAttributes.add(attribute.getAttributeName());
					continue;
				}
				
				//System.out.println(	attribute.getAttributeName()+"="+value.getStringValue());
				
				// find the value according to the dictionnary
				if (value.getStringValue().trim().isEmpty()) {
					// this value is empty in the shapefile
					attribute2value.put(gosplType, gosplType.getEmptyValue());
				}
				try {
					IValue valueEncoded;
					// if the value is valid...
					if (gosplType.getValueSpace().isValidCandidate(value.getStringValue())) {
						// ... then we use the encoded value as defined in the attribute
						try {
							valueEncoded = gosplType.getValueSpace().getValue(value.getStringValue());
						} catch (NullPointerException e) {
							valueEncoded = gosplType.getValueSpace().addValue(value.getStringValue());	
						}
					} else {
						valueEncoded = gosplType.getValueSpace().addValue(value.getStringValue());
						/*
						Set<String> ignoredValuesSet = attributeName2ignoredValues.get(gosplType.getAttributeName());
						if (ignoredAttributes == null) {
							ignoredValuesSet = new HashSet<>();
							attributeName2ignoredValues.put(gosplType.getAttributeName(), ignoredValuesSet);
						}
						ignoredValuesSet.add(value.getStringValue());*/
					}
					attribute2value.put(gosplType, valueEncoded);

				} catch (RuntimeException e) {
					System.err.println("error while decoding values: "+e.getMessage());
					e.printStackTrace();
				}
				
			}
			
			// add the resulting entity to this population
			GosplEntity entity = new GosplEntity(attribute2value);
			add(new SpllEntity(entity, feature.getGeometry()));
			this.attributes.addAll(entity.getAttributes());
			if (maxEntities > 0 && ++i >= maxEntities)
				break;
		}
		
		if (!ignoredAttributes.isEmpty())
			System.err.println("the following attributes were ignored because "+
								"they are not defined in the dictionnary: "+ignoredAttributes);
		
	}
	
	/**
	 * Gives the specific coordinate system this population
	 * have been localized with
	 * 
	 * @return
	 */
	public CoordinateReferenceSystem getCrs(){
		return SpllUtil.getCRSfromWKT(geoFile.getWKTCoordinateReferentSystem());
	}
	
	/**
	 * Gives the geography this population is localized in
	 * 
	 * @return
	 */
	public IGSGeofile<? extends AGeoEntity<? extends IValue>, IValue> getGeography() {
		return geoFile;
	}
	
	@Override
	public Set<DemographicAttribute<? extends IValue>> getPopulationAttributes() {
		return Collections.unmodifiableSet(attributes);
	}
	
	// ------------------------------------------- //
	// ----------- COLLECTION CONTRACT ----------- //
	// ------------------------------------------- //
	
	
	@Override
	public int size() {
		return population.size();
	}

	@Override
	public boolean isEmpty() {
		return population.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return population.contains(o);
	}

	@Override
	public Iterator<SpllEntity> iterator() {
		return population.iterator();
	}

	@Override
	public Object[] toArray() {
		return population.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return population.toArray(a);
	}

	@Override
	public boolean add(SpllEntity e) {
		return population.add(e);
	}

	@Override
	public boolean remove(Object o) {
		return population.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return population.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends SpllEntity> c) {
		return population.addAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return population.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return population.retainAll(c);
	}

	@Override
	public void clear() {
		population.clear();
	}
	
	

}
