package spll.entity.iterator;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;

import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.GeometryCoordinateSequenceTransformer;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Geometry;

import spll.entity.GeoEntityFactory;
import spll.entity.SpllFeature;

public class GSFeatureIterator implements Iterator<SpllFeature> {

	private GeoEntityFactory factory;

	private FeatureIterator<SimpleFeature> fItt;

	private GeometryCoordinateSequenceTransformer transformer;
	private CoordinateReferenceSystem crs;

	public GSFeatureIterator(DataStore dataStore, Filter filter) {
		this.fItt = null;
		try {
			this.fItt = DataUtilities.collection(dataStore.getFeatureSource(dataStore.getTypeNames()[0])
					.getFeatures(filter)).features();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.factory = new GeoEntityFactory();
	}
	
	public GSFeatureIterator(DataStore dataStore) {
		this(dataStore, Filter.INCLUDE);
	}

	public GSFeatureIterator(DataStore dataStore, CoordinateReferenceSystem crs) throws FactoryException, IOException {
		this(dataStore);
		this.crs = crs;
		GeometryCoordinateSequenceTransformer morph = new GeometryCoordinateSequenceTransformer();
		morph.setMathTransform(CRS.findMathTransform(dataStore.getFeatureSource(dataStore.getTypeNames()[0])
				.getSchema().getCoordinateReferenceSystem(), crs));
		this.transformer = morph;
	}

	@Override
	public boolean hasNext() {
		return fItt.hasNext();
	}

	@Override
	public SpllFeature next() {
		SimpleFeature feature = fItt.next(); 
		if(transformer != null){
			SimpleFeatureType schema = null;
			try {
				schema = DataUtilities.createSubType(feature.getFeatureType(), null, this.crs);
			} catch (SchemaException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			feature = SimpleFeatureBuilder.template(schema, feature.getID());

			try {
				feature.setDefaultGeometry(transformer.transform((Geometry) feature.getDefaultGeometry()));
			} catch (TransformException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return factory.createGeoEntity(feature, Collections.emptyList());
	}

}
