package spll.io;

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

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.type.BasicFeatureTypes;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.index.quadtree.Quadtree;

import core.metamodel.attribute.geographic.GeographicAttribute;
import core.metamodel.entity.AGeoEntity;
import core.metamodel.io.IGSGeofile;
import core.metamodel.value.IValue;
import spll.entity.GeoEntityFactory;
import spll.entity.SpllFeature;
import spll.entity.iterator.GSFeatureIterator;
import spll.io.exception.InvalidGeoFormatException;
import spll.util.SpllGeotoolsAdapter;
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
public class SPLVectorFile implements IGSGeofile<SpllFeature, IValue> {

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
		this.features = features;
		SimpleFeatureType schema = dataStore.getSchema(dataStore.getTypeNames()[0]);
		this.crs = schema.getCoordinateReferenceSystem();
		FeatureIterator<SimpleFeature> fItt = DataUtilities.collection(dataStore
				.getFeatureSource(dataStore.getTypeNames()[0]).getFeatures(Filter.INCLUDE))
				.features();

		// Tests whether the dataStore contains all SimpleFeature that are within the GSFeature set
		// WARNING: for weird reasons, when a Feature is stored in a DataStore every numerical value
		// are transposed to a double value; hence fail to recognize equality with set integer value
		/*while(fItt.hasNext()){
			SimpleFeature feat = fItt.next();
			for(Property prop : feat.getProperties()){
				boolean match = false;
				for(SpllFeature feature : features){
					if(prop.getName().toString().equals(BasicFeatureTypes.GEOMETRY_ATTRIBUTE_NAME)){
						if(prop.getValue().toString().equals(feature.getGeometry().toString()))
							match = true;
					} else {
						IValue val = feature.getValueForAttribute(prop.getName().toString());
						if(val.getValueSpace().getType().isNumericValue()
					
							&& prop.getValue().toString().equals(feature.getInnerFeature()
									.getProperty(prop.getName()).getValue().toString())){
							match = true;
						}
					}
				}
				if(!match)
					throw new IllegalArgumentException("Property "+prop.getName().toString()+" has not been match at all:\n"
							+ "Geotools feature value is "+feat.getProperty(prop.getName()).getValue().toString()+ "\n"
							+ "but available value are: "
							+ features.stream().map(gsf -> gsf.getInnerFeature().getProperty(prop.getName()).toString())
							.collect(Collectors.joining("; ")));
			}	
		}*/
		
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
		GeoEntityFactory gef = new GeoEntityFactory();
		while (fItt.hasNext())
			features.add(gef.createGeoEntity(fItt.next(), attributes));
	}

	protected SPLVectorFile(File file, Charset charset, List<String> attributes) throws IOException{
		this(readDataStoreFromFile(file, charset), attributes);
	}

	protected SPLVectorFile(File file, Charset charset) throws IOException{
		this(readDataStoreFromFile(file, charset), Collections.emptyList());
	}

	private static DataStore readDataStoreFromFile(File file, Charset charset) throws IOException {
		
		Map<String,Object> parameters = new HashMap<>();
		parameters.put("url", file.toURI().toURL());
		
		DataStore datastore = DataStoreFinder.getDataStore(parameters);
		
		// set the charset (if possible)
		if (charset != null 
			&& datastore instanceof ShapefileDataStore) {
			((ShapefileDataStore)datastore).setCharset(charset);
		}
		
		return datastore;
	}
	
	// ------------------- GENERAL CONTRACT ------------------- //

	@Override
	public GeoGSFileType getGeoGSFileType() {
		return GeoGSFileType.VECTOR;
	}

	@Override
	public boolean isCoordinateCompliant(IGSGeofile<? extends AGeoEntity<? extends IValue>, ? extends IValue> file) {
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
	
	@Override
	public IGSGeofile<SpllFeature, IValue> transferTo(File destination,
			Map<? extends AGeoEntity<? extends IValue>,Number> transfer,
			GeographicAttribute<? extends IValue> attribute) throws IllegalArgumentException, IOException {
		if(features.stream().anyMatch(feat -> !transfer.containsKey(feat)))
			throw new IllegalArgumentException("There is a mismatch between provided set of geographical entity and "
					+ "geographic entity of this SPLVector file "+this.toString());
		
		Set<GeographicAttribute<? extends IValue>> attrSet = new HashSet<GeographicAttribute<? extends IValue>>();
		attrSet.add(attribute);
		GeoEntityFactory gef = new GeoEntityFactory(
				attrSet, 
				SpllGeotoolsAdapter.getInstance().getGeotoolsFeatureType(
						attribute.toString(), 
						attrSet, 
						this.crs, 
						this.dataStore.getFeatureSource(dataStore.getTypeNames()[0]).getSchema().getGeometryDescriptor()));
		
		Collection<SpllFeature> newFeatures = new HashSet<>();
		for(AGeoEntity<? extends IValue> entity : this.features) {
			Map<GeographicAttribute<? extends IValue>, IValue> theMap = new HashMap<>();
			theMap.put(attribute, attribute.getValueSpace().getInstanceValue(transfer.get(entity).toString()));
			newFeatures.add(gef.createGeoEntity(entity.getGeometry(), theMap));
		}
		
		IGSGeofile<SpllFeature, IValue> res = null;
		try {
			res =  new SPLGeofileBuilder().setFeatures(newFeatures).setFile(destination).buildShapeFile();
		} catch (SchemaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidGeoFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
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
	 * TODO: leave parallel processing option open
	 * 
	 * @return
	 */
	@Override
	public Collection<GeographicAttribute<? extends IValue>> getGeoAttributes() {
		return features.stream().flatMap(f -> f.getAttributes().stream())
				.collect(Collectors.toSet());
	}

	/**
	 * {@inheritDoc}
	 * 
	 * TODO: leave parallel processing option open
	 * 
	 * @return
	 */
	@Override
	public Collection<IValue> getGeoValues() {
		return features.stream().flatMap(f -> f.getValues().stream())
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
	
	public SPLVectorFile applyBuffer(Double minDist, Double maxDist, Boolean avoidOverlapping, String destination) throws IOException, SchemaException, InvalidGeoFormatException {
		Set<SpllFeature> fts = new HashSet<>();
		SimpleFeatureType schemaOld = this.getStore().getSchema(this.getStore().getNames().get(0));
		
		final StringBuilder specs = new StringBuilder();
		specs.append("geometry:MultiPolygon"); 
		for (final AttributeType at : schemaOld.getTypes()) {
			if (Geometry.class.isAssignableFrom(at.getBinding())) continue;
			String name = at.getName().toString().replaceAll("\"", "");
			name = name.replaceAll("'", "");
			specs.append(',').append(name).append(":String");
		}
		SimpleFeatureType schemNew = DataUtilities.createType("buffer",specs.toString());
		schemNew = DataUtilities.createSubType( schemNew, null, schemaOld.getCoordinateReferenceSystem() );
		
		
		GeoEntityFactory gef = new GeoEntityFactory((Set<GeographicAttribute<? extends IValue>>) getGeoAttributes(),schemNew );
		
		Quadtree quadTreeMin = null;
		if (minDist != null && minDist >= 0) {
			quadTreeMin = new Quadtree();
			for (SpllFeature ft : features) {
				Geometry g = ft.getGeometry().buffer(minDist);
				quadTreeMin.insert(g.getEnvelopeInternal(), g);
			}
		}
		for (SpllFeature ft : features) {
			 SimpleFeatureBuilder builder = new SimpleFeatureBuilder((SimpleFeatureType) ft.getInnerFeature().getType());
			 Geometry newGeom = ft.getGeometry().buffer(maxDist);
			 if (quadTreeMin != null && ! quadTreeMin.isEmpty()) {
				 List<Geometry> intersection = quadTreeMin.query(newGeom.getEnvelopeInternal());
				 for (Geometry g : intersection) {
					 newGeom =  newGeom.difference(g);
				 }
				 newGeom.buffer(0.0);
			 }
			fts.add(gef.createGeoEntity(newGeom, ft.getAttributeMap()));
		}
		
		if (avoidOverlapping) {
			
		}
		SPLGeofileBuilder builder = new SPLGeofileBuilder();
		builder.setFeatures(fts);
		builder.setFile(new File(destination));
		return builder.buildShapeFile();
	}

	public String toString() {
		String s = "";
		try {
			s = "Shapefile containing "+features.size()+" features of geometry type "+dataStore
					.getSchema(dataStore.getTypeNames()[0]).getGeometryDescriptor().getType();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return s;
	}

}
