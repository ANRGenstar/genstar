package io.geofile;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import io.datareaders.iterator.GSFeatureIterator;
import io.geofile.data.GSFeature;

public class ShapeFile implements IGSGeofile {

	private Set<GSFeature> features = null;
	
	private final DataStore dataStore;
	private final CoordinateReferenceSystem crs;
	
	protected ShapeFile() {
		this.dataStore = null;
		this.crs = null;
	}
	
	protected ShapeFile(File file) throws IOException{
	    Map<String, Object> map = new HashMap<String, Object>();
	    map.put("url", file.toURI().toURL());

	    this.dataStore = DataStoreFinder.getDataStore(map);
	    this.crs = dataStore.getSchema(dataStore.getTypeNames()[0]).getCoordinateReferenceSystem();

	    FeatureSource<SimpleFeatureType,SimpleFeature> fSource = dataStore
	            .getFeatureSource(dataStore.getTypeNames()[0]);
	    Filter filter = Filter.INCLUDE; // ECQL.toFilter("BBOX(THE_GEOM, 10,20,30,40)")

	    features = new HashSet<>();
	    FeatureCollection<SimpleFeatureType, SimpleFeature> collection = fSource.getFeatures(filter);
	    FeatureIterator<SimpleFeature> fItt = collection.features();
	    while (fItt.hasNext())
	    	features.add(new GSFeature(fItt.next()));
	}
	
	@Override
	public GeoGSFileType getGeoGSFileType() {
		return GeoGSFileType.VECTOR;
	}
	
	/**
	 * Immutable list of features
	 * 
	 * @return
	 */
	@Override
	public Collection<GSFeature> getGeoData() {
		return Collections.unmodifiableSet(features);
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
	
	public Iterator<GSFeature> getGeoAttributeIterator(GSFeature feature) {
		return new GSFeatureIterator(dataStore, feature);
	}
	
}
