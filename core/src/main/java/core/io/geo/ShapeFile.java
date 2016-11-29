package core.io.geo;

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
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.type.BasicFeatureTypes;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;

import au.com.bytecode.opencsv.CSVReader;
import core.io.geo.entity.GSFeature;
import core.io.geo.entity.GeoEntityFactory;
import core.io.geo.entity.attribute.AGeoAttribute;
import core.io.geo.entity.attribute.RawGeoAttribute;
import core.io.geo.entity.attribute.value.AGeoValue;
import core.io.geo.entity.attribute.value.RawGeoData;
import core.io.geo.iterator.GSFeatureIterator;

public class ShapeFile implements IGSGeofile {

	private Set<GSFeature> features = null;
	
	private final DataStore dataStore;
	private final CoordinateReferenceSystem crs;
	
	/*
	 * Protected constructor to ensure file are created through the provided factory
	 */
	protected ShapeFile(DataStore dataStore) throws IOException {
		this.dataStore = dataStore;
		this.crs = dataStore.getSchema(dataStore.getTypeNames()[0]).getCoordinateReferenceSystem();
		FeatureSource<SimpleFeatureType,SimpleFeature> fSource = dataStore
	            .getFeatureSource(dataStore.getTypeNames()[0]);
		Filter filter = Filter.INCLUDE;
		features = new HashSet<>();
	    FeatureIterator<SimpleFeature> fItt = DataUtilities.collection(fSource.getFeatures(filter)).features();
	    GeoEntityFactory gef = new GeoEntityFactory(new HashSet<AGeoAttribute>());
	    while (fItt.hasNext())
	    	features.add(gef.createGeoEntity(fItt.next()));
	}
	
	protected ShapeFile(File file) throws IOException{
		this(DataStoreFinder.getDataStore(
				Stream.of(
						new AbstractMap.SimpleEntry<String, URL>("url", file.toURI().toURL()))
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue)))
		);
	}
	
	@Override
	public GeoGSFileType getGeoGSFileType() {
		return GeoGSFileType.VECTOR;
	}
	

	@Override
	public Collection<GSFeature> getGeoData() {
		return Collections.unmodifiableSet(features);
	}
	
	@Override
	public Collection<AGeoValue> getGeoValues() {
		return features.parallelStream().flatMap(f -> f.getValues().stream()).collect(Collectors.toSet());
	}

	@Override
	public Envelope getEnvelope() throws IOException {
		return dataStore.getFeatureSource(dataStore.getTypeNames()[0]).getBounds();
	}
	
	@Override
	public boolean isCoordinateCompliant(IGSGeofile file) {
		return file.getCoordRefSystem().equals(this.getCoordRefSystem());
	}

	@Override
	public CoordinateReferenceSystem getCoordRefSystem() {
		return crs;
	}

	@Override
	public Iterator<GSFeature> getGeoAttributeIterator() {
		return new GSFeatureIterator(dataStore);
	}
	
	@Override
	public Iterator<GSFeature> getGeoAttributeIterator(CoordinateReferenceSystem crs) throws FactoryException, IOException {
		return new GSFeatureIterator(dataStore, crs);
	}
	
	@Override
	public Iterator<GSFeature> getGeoAttributeIteratorWithin(Geometry geom) {
		Filter filter = CommonFactoryFinder.getFilterFactory2()
				.within(BasicFeatureTypes.GEOMETRY_ATTRIBUTE_NAME, 
						(org.opengis.geometry.Geometry) geom);
		return new GSFeatureIterator(dataStore, filter);
	}
	

	@Override
	public Collection<GSFeature> getGeoDataWithin(Geometry geom) {
		Set<GSFeature> collection = new HashSet<>(); 
		getGeoAttributeIteratorWithin(geom).forEachRemaining(collection::add);
		return collection;
	}
	
	@Override
	public Iterator<GSFeature> getGeoAttributeIteratorIntersect(Geometry geom) {
		Filter filter = CommonFactoryFinder.getFilterFactory2()
				.intersects(BasicFeatureTypes.GEOMETRY_ATTRIBUTE_NAME, 
						(org.opengis.geometry.Geometry) geom);
		return new GSFeatureIterator(dataStore, filter);
	}
	
	@Override
	public Collection<GSFeature> getGeoDataIntersect(Geometry geom) {
		Set<GSFeature> collection = new HashSet<>(); 
		getGeoAttributeIteratorIntersect(geom).forEachRemaining(collection::add);
		return collection;
	}
	
	public DataStore getStore() {
		return dataStore;
	}
	
	public void addAttributes(File csvFile, char seperator, String keyAttribute, String keyCSV, Map<String,String> newAttributes) {
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
			Map<String, Integer> attIndexTmp = newAttributes.keySet().stream().collect(Collectors.toMap(s -> s, s -> names.contains(s) ? names.indexOf(s) : - 1));
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
					
			for (GSFeature ft : features) {
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
