package spll.io.file;

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
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;

public class ShapeFileIO implements ISPLFileIO<Feature> {

	private List<Geometry> geoms = null;
	private List<Feature> features = null;
	
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
	    geoms = new ArrayList<>();
	    FeatureCollection<SimpleFeatureType, SimpleFeature> collection = fSource.getFeatures(filter);
	    try (FeatureIterator<SimpleFeature> fItt = collection.features()) {
	        while (fItt.hasNext()) {
	            SimpleFeature feature = fItt.next();
	            features.add(feature);
	            geoms.add((Geometry) feature.getDefaultGeometry());
	        }
	    }
	}
	
	/**
	 * Immutable list of features
	 * 
	 * @return
	 */
	@Override
	public List<Feature> getFeatures() {
		return Collections.unmodifiableList(features);
	}
	
	/**
	 * Immutable list of geometry
	 * 
	 * @return
	 */
	public List<Geometry> getGeometry() {
		return Collections.unmodifiableList(geoms);
	}
	
	/**
	 * Return the feature accessor 
	 * 
	 * @return
	 * @throws IOException
	 */
	public FeatureSource<SimpleFeatureType,SimpleFeature> getFeatureSource() throws IOException{
		return dataStore.getFeatureSource(dataStore.getTypeNames()[0]);
	}
	
	/**
	 * TODO: javadoc
	 * 
	 * @return
	 * @throws IOException 
	 */
	public SimpleFeatureType getFeatureType() throws IOException {
		return this.dataStore.getSchema(dataStore.getTypeNames()[0]);
	}

	@Override
	public boolean isCoordinateCompliant(ISPLFileIO<Feature> file) {
		return file.getCoordRefSystem().equals(this.getCoordRefSystem());
	}

	@Override
	public CoordinateReferenceSystem getCoordRefSystem() {
		return crs;
	}
	
}
