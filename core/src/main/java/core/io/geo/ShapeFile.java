package core.io.geo;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.type.BasicFeatureTypes;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;

import core.io.geo.entity.GSFeature;
import core.io.geo.entity.GeoEntityFactory;
import core.io.geo.entity.attribute.AGeoAttribute;
import core.io.geo.entity.attribute.value.AGeoValue;
import core.io.geo.iterator.GSFeatureIterator;

public class ShapeFile implements IGSGeofile {

	private Set<GSFeature> features = null;
	
	private final DataStore dataStore;
	private final CoordinateReferenceSystem crs;
	
	protected ShapeFile(DataStore dataStore) throws IOException {
		this.dataStore = dataStore;
		this.crs = dataStore.getSchema(dataStore.getTypeNames()[0]).getCoordinateReferenceSystem();
		FeatureSource<SimpleFeatureType,SimpleFeature> fSource = dataStore
	            .getFeatureSource(dataStore.getTypeNames()[0]);
		Filter filter = Filter.INCLUDE;
		features = new HashSet<>();
	    FeatureCollection<SimpleFeatureType, SimpleFeature> collection = fSource.getFeatures(filter);
	    FeatureIterator<SimpleFeature> fItt = collection.features();
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
	public Iterator<GSFeature> getGeoAttributeIteratorIntersect(Geometry geom) {
		Filter filter = CommonFactoryFinder.getFilterFactory2()
				.intersects(BasicFeatureTypes.GEOMETRY_ATTRIBUTE_NAME, 
						(org.opengis.geometry.Geometry) geom);
		return new GSFeatureIterator(dataStore, filter);
	}
	
	public DataStore getStore() {
		return dataStore;
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
