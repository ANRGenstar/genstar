package spll.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.type.BasicFeatureTypes;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import au.com.bytecode.opencsv.CSVReader;
import core.metamodel.geo.AGeoAttribute;
import core.metamodel.geo.AGeoEntity;
import core.metamodel.geo.AGeoValue;
import core.metamodel.geo.io.GeoGSFileType;
import core.metamodel.geo.io.IGSGeofile;
import spll.entity.SpllFeature;
import spll.entity.GeoEntityFactory;
import spll.entity.attribute.RawGeoAttribute;
import spll.entity.attribute.value.RawGeoData;
import spll.entity.iterator.GSFeatureIterator;
import spll.util.SpllUtil;

/**
 * TODO: javadoc
 * 
 * WARNING: purpose of this file is to cover the wider number of template for geographic vector files,
 * <i>but</i> in practice it can only stand for standard shapefile
 * 
 * @author kevinchapuis
 *
 */
public class SPLVectorFile implements IGSGeofile<SpllFeature> {

	private Set<SpllFeature> features = null;

	private final DataStore dataStore;
	private final CoordinateReferenceSystem crs;

	/**
	 * In this constructor {@link SpllFeature} and {@code dataStore} provide the side of the
	 * same coin: {@link SpllFeature} set must contains all {@link Feature} of the {@code dataStore}
	 * 
	 * @param dataStore
	 * @param features
	 * @throws IOException
	 */
	protected SPLVectorFile(DataStore dataStore, Set<SpllFeature> features) throws IOException {
		this.dataStore = dataStore;
		SimpleFeatureType schema = dataStore.getSchema(dataStore.getTypeNames()[0]);
		this.crs = schema.getCoordinateReferenceSystem();
		FeatureIterator<SimpleFeature> fItt = DataUtilities.collection(dataStore
				.getFeatureSource(dataStore.getTypeNames()[0]).getFeatures(Filter.INCLUDE))
				.features();

		// Tests whether the dataStore contains all SimpleFeature that are within the GSFeature set
		// WARNING: for weird reasons, when a Feature is stored in a DataStore every numerical value
		// are transposed to a double value; hence fail to recognize equality with set integer value
		while(fItt.hasNext()){
			SimpleFeature feat = fItt.next();
			for(Property prop : feat.getProperties()){
				boolean match = false;
				for(SpllFeature feature : features){
					if(prop.getName().toString().equals(BasicFeatureTypes.GEOMETRY_ATTRIBUTE_NAME)){
						if(prop.getValue().toString().equals(feature.getGeometry().toString()))
							match = true;
					} else if(feature.getValueForAttribute(prop.getName().toString()).isNumericalValue()
							&& Double.valueOf(prop.getValue().toString()).equals(
									feature.getValueForAttribute(prop.getName().toString())
									.getNumericalValue().doubleValue())
							|| prop.getValue().toString().equals(feature.getInnerFeature()
									.getProperty(prop.getName()).getValue().toString())){
						match = true;
					}
				}
				if(!match)
					throw new IllegalArgumentException("Property "+prop.getName().toString()+" has not been match at all:\n"
							+ "Geotools feature value is "+feat.getProperty(prop.getName()).getValue().toString()+ "\n"
							+ "but available value are: "
							+ features.stream().map(gsf -> gsf.getInnerFeature().getProperty(prop.getName()).toString())
							.collect(Collectors.joining("; ")));
			}	
		}
		this.features = features;
	}

	/**
	 * In this constructor {@link SpllFeature} are build from the {@link Feature} contains in the {@code dataStore}
	 * 
	 * @param dataStore
	 * @param attributes
	 * @throws IOException
	 */
	protected SPLVectorFile(DataStore dataStore, List<String> attributes) throws IOException {
		this.dataStore = dataStore;
		this.crs = dataStore.getSchema(dataStore.getTypeNames()[0]).getCoordinateReferenceSystem();
		FeatureSource<SimpleFeatureType,SimpleFeature> fSource = dataStore
				.getFeatureSource(dataStore.getTypeNames()[0]);
		features = new HashSet<>();
		FeatureIterator<SimpleFeature> fItt = DataUtilities.collection(fSource.getFeatures(Filter.INCLUDE)).features();
		GeoEntityFactory gef = new GeoEntityFactory(new HashSet<AGeoAttribute>());
		while (fItt.hasNext())
			features.add(gef.createGeoEntity(fItt.next(), attributes));
	}

	protected SPLVectorFile(File file, List<String> attributes) throws IOException{
		this(DataStoreFinder.getDataStore(
				Stream.of(
						new AbstractMap.SimpleEntry<String, URL>("url", file.toURI().toURL()))
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue))), attributes
				);
	}

	protected SPLVectorFile(File file) throws IOException{
		this(DataStoreFinder.getDataStore(
				Stream.of(
						new AbstractMap.SimpleEntry<String, URL>("url", file.toURI().toURL()))
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue))), Collections.emptyList()
				);
	}

	// ------------------- GENERAL CONTRACT ------------------- //

	@Override
	public GeoGSFileType getGeoGSFileType() {
		return GeoGSFileType.VECTOR;
	}

	@Override
	public boolean isCoordinateCompliant(IGSGeofile<? extends AGeoEntity> file) {
		CoordinateReferenceSystem thisCRS = null, fileCRS = null;
		thisCRS = SpllUtil.getCRSfromWKT(this.getWKTCoordinateReferentSystem());
		fileCRS = SpllUtil.getCRSfromWKT(file.getWKTCoordinateReferentSystem());
		if (thisCRS == null && fileCRS == null) return false;
		if (thisCRS.equals(fileCRS)) return true;
		Integer codeThis = null;
		Integer codeFile = null;
		try {
			codeThis = CRS.lookupEpsgCode(thisCRS, true);
			codeFile = CRS.lookupEpsgCode(fileCRS, true);
		} catch (FactoryException e) {
			e.printStackTrace();
		}
		return codeThis == null && codeFile == null ? false : codeFile.equals(codeThis) ;
	}

	@Override
	public String getWKTCoordinateReferentSystem() {
		return crs.toWKT();
	}

	@Override
	public Envelope getEnvelope() throws IOException {
		return new ReferencedEnvelope(dataStore.getFeatureSource(dataStore.getTypeNames()[0]).getBounds());
	}

	// ---------------------------------------------------------------- //
	// ----------------------- ACCESS TO VALUES ----------------------- //
	// ---------------------------------------------------------------- //


	@Override
	public Collection<SpllFeature> getGeoEntity() {
		return Collections.unmodifiableSet(features);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * WARNING: make use of parallelism through {@link Stream#isParallel()}
	 * @return
	 */
	@Override
	public Collection<AGeoAttribute> getGeoAttributes() {
		return features.parallelStream().flatMap(f -> f.getAttributes().stream())
				.collect(Collectors.toSet());
	}

	/**
	 * {@inheritDoc}
	 * 
	 * WARNING: make use of parallelism through {@link Stream#parallel()}
	 * @return
	 */
	@Override
	public Collection<AGeoValue> getGeoValues() {
		return features.parallelStream().flatMap(f -> f.getValues().stream())
				.collect(Collectors.toSet());
	}

	@Override
	public Iterator<SpllFeature> getGeoEntityIterator() {
		return new GSFeatureIterator(dataStore);
	}

	@Override
	public Iterator<SpllFeature> getGeoEntityIteratorWithin(Geometry geom) {
		FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2( GeoTools.getDefaultHints() );
		Filter filter = ff.within(ff.property( BasicFeatureTypes.GEOMETRY_ATTRIBUTE_NAME), ff.literal( geom ));
		return new GSFeatureIterator(dataStore, filter);
	}


	@Override
	public Collection<SpllFeature> getGeoEntityWithin(Geometry geom) {
		Set<SpllFeature> collection = new HashSet<>(); 
		getGeoEntityIteratorWithin(geom).forEachRemaining(collection::add);
		return collection;
	}

	@Override
	public Iterator<SpllFeature> getGeoEntityIteratorIntersect(Geometry geom) {
		FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2( GeoTools.getDefaultHints() );
		Filter filter = ff.intersects(ff.property( BasicFeatureTypes.GEOMETRY_ATTRIBUTE_NAME), ff.literal( geom ));
		return new GSFeatureIterator(dataStore, filter);
	}

	@Override
	public Collection<SpllFeature> getGeoEntityIntersect(Geometry geom) {
		Set<SpllFeature> collection = new HashSet<>(); 
		getGeoEntityIteratorIntersect(geom).forEachRemaining(collection::add);
		return collection;
	}

	public DataStore getStore() {
		return dataStore;
	}

	public void addAttributes(File csvFile, char seperator, String keyAttribute, String keyCSV, List<String> newAttributes) {
		if (features== null && features.isEmpty()) return;
		if (!csvFile.exists()) return;
		try {
			CSVReader reader = new CSVReader(new FileReader(csvFile), seperator);
			List<String[]> dataTable = reader.readAll();
			reader.close();
			Map<String, Map<String, String>> values = new Hashtable<String, Map<String, String>>();
			List<String> names = Arrays.asList(dataTable.get(0));
			int keyCSVind = names.contains(keyCSV) ? names.indexOf(keyCSV) : -1;
			if (keyCSVind == -1) return;
			Map<String, Integer> attIndexTmp = newAttributes.stream().collect(Collectors.toMap(s -> s, s -> names.contains(s) ? names.indexOf(s) : - 1));
			Map<String, Integer> attIndex = new Hashtable<String, Integer>();
			Map<String, AGeoAttribute> attAGeo = new Hashtable<String, AGeoAttribute>();
			for (String n : attIndexTmp.keySet()) {
				int id = attIndexTmp.get(n);
				if (id != -1) {
					attIndex.put(n, id);
					attAGeo.put(n, new RawGeoAttribute(n));
				}
			}

			for (String[] data : dataTable) {
				String id = data[keyCSVind];
				Map<String, String> vals = new Hashtable<String, String>();
				for (String name : attIndex.keySet()) {
					int idat = attIndex.get(name);
					vals.put(name, data[idat]);
				}
				values.put(id, vals);
			}
			NumberFormat defaultFormat = NumberFormat.getInstance();
			for (SpllFeature ft : features) {
				Collection<String> properties = ft.getPropertiesAttribute();
				if (!properties.contains(keyAttribute)) continue;
				String objid = ft.getValueForAttribute(keyAttribute).getStringValue();

				Map<String, String> vals = values.get(objid);
				if (vals == null) continue;
				for (String vN : vals.keySet()) {
					AGeoAttribute attri = attAGeo.get(vN);

					String v = vals.get(vN);
					try {
						Number value = defaultFormat.parse(v);
						ft.addAttribute(attri, new RawGeoData(attri,value));
					} catch (ParseException e){
						ft.addAttribute(attri, new RawGeoData(attri,  v));
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String toString() {
		String s = "";
		try {
			s = "Shapefile containing "+features.size()+" features of geometry type "+dataStore.getSchema(dataStore.getTypeNames()[0]).getGeometryDescriptor().getType();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return s;
	}

}
