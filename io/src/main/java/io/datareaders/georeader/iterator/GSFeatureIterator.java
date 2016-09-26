package io.datareaders.georeader.iterator;

import java.io.IOException;
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

import io.datareaders.georeader.geodat.GSFeature;

public class GSFeatureIterator implements Iterator<GSFeature> {

	private FeatureIterator<SimpleFeature> fItt;
	
	private GeometryCoordinateSequenceTransformer transformer;
	private CoordinateReferenceSystem crs;
	
	public GSFeatureIterator(DataStore dataStore) {
		this.fItt = null;
		try {
			this.fItt = dataStore.getFeatureSource(dataStore.getTypeNames()[0]).getFeatures(Filter.INCLUDE).features();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public GSFeatureIterator(DataStore dataStore, CoordinateReferenceSystem crs) throws FactoryException, IOException {
		this(dataStore);
		this.crs = crs;
		GeometryCoordinateSequenceTransformer morph = new GeometryCoordinateSequenceTransformer();
		morph.setMathTransform(CRS.findMathTransform(dataStore.getFeatureSource(dataStore.getTypeNames()[0]).getSchema().getCoordinateReferenceSystem(), crs));
		this.transformer = morph;
	}

	@Override
	public boolean hasNext() {
		return fItt.hasNext();
	}

	@Override
	public GSFeature next() {
		SimpleFeature feature = fItt.next();
		if(transformer != null)
			return new GSFeature(feature);
		SimpleFeatureType schema = null;
		try {
			schema = DataUtilities.createSubType(feature.getFeatureType(), null, this.crs);
		} catch (SchemaException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        SimpleFeature copy = SimpleFeatureBuilder.template(schema, feature.getID());

        try {
			copy.setDefaultGeometry(transformer.transform((Geometry) feature.getDefaultGeometry()));
		} catch (TransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new GSFeature(copy);
	}

}
