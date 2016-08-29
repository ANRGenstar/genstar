package io.datareaders.georeader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import io.datareaders.georeader.geodat.GSFeature;

public class ShapeFileIO implements IGeoGSFileIO<GSFeature, Property, Object> {

	private List<GSFeature> features = null;
	
	private final DataStore dataStore;
	private final CoordinateReferenceSystem crs;
	
	/**
	 * TODO: expend to enable more option at object creation (e.g. Filter and FeatureType)
	 * 
	 * @param path
	 * @throws IOException
	 */
	public ShapeFileIO(String path) throws IOException{
		File file = new File(path);
	    Map<String, Object> map = new HashMap<String, Object>();
	    map.put("url", file.toURI().toURL());

	    this.dataStore = DataStoreFinder.getDataStore(map);
	    this.crs = dataStore.getSchema(dataStore.getTypeNames()[0]).getCoordinateReferenceSystem();

	    FeatureSource<SimpleFeatureType,SimpleFeature> fSource = dataStore
	            .getFeatureSource(dataStore.getTypeNames()[0]);
	    Filter filter = Filter.INCLUDE; // ECQL.toFilter("BBOX(THE_GEOM, 10,20,30,40)")

	    features = new ArrayList<>();
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
	public List<GSFeature> getGeoData() {
		return Collections.unmodifiableList(features);
	}

	@Override
	public boolean isCoordinateCompliant(IGeoGSFileIO<GSFeature, Property, Object> file) {
		return file.getCoordRefSystem().equals(this.getCoordRefSystem());
	}

	@Override
	public CoordinateReferenceSystem getCoordRefSystem() {
		return crs;
	}
	
}
