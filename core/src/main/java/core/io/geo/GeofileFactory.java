package core.io.geo;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.SchemaException;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import core.io.exception.InvalidFileTypeException;
import core.io.geo.entity.GSFeature;

public class GeofileFactory {

	private static String SHAPEFILE_EXT = ".shp";
	private static String GEOTIFF_EXT = ".tif";
	
	private final List<String> supportedFileFormat;
	
	public GeofileFactory(){
		supportedFileFormat = Arrays.asList(SHAPEFILE_EXT, GEOTIFF_EXT);
	}
	
	/**
	 * 
	 * Create a geo referenced file 
	 * 
	 * @param geofile
	 * @return
	 * @throws IllegalArgumentException
	 * @throws TransformException
	 * @throws IOException
	 * @throws InvalidFileTypeException
	 */
	public IGSGeofile getGeofile(File geofile) 
			throws IllegalArgumentException, TransformException, IOException, InvalidFileTypeException{
		if(geofile.getName().contains(SHAPEFILE_EXT))
			return new ShapeFile(geofile);
		if(geofile.getName().contains(GEOTIFF_EXT))
			return new GeotiffFile(geofile);
		String[] pathArray = geofile.getPath().split(File.separator);
		throw new InvalidFileTypeException(pathArray[pathArray.length-1], supportedFileFormat);
	}
	
	/**
	 * Create a geotiff file based on a collection of pixel data 
	 * 
	 * @param rasterfile
	 * @param pixels
	 * @return
	 * @throws IOException
	 * @throws IllegalArgumentException
	 * @throws TransformException
	 */
	public GeotiffFile getGeofile(File rasterfile, float[][] pixels) 
			throws IOException, IllegalArgumentException, TransformException {
		return getGeofile(rasterfile, pixels, DefaultGeographicCRS.WGS84);
	}
	
	/**
	 * TODO
	 * 
	 * @param rasterfile
	 * @param pixels
	 * @param crs
	 * @return
	 * @throws IOException
	 * @throws IllegalArgumentException
	 * @throws TransformException
	 */
	public GeotiffFile getGeofile(File rasterfile, float[][] pixels, CoordinateReferenceSystem crs) 
			throws IOException, IllegalArgumentException, TransformException {
		
		// Create image options based on pixels' characteristics
		ReferencedEnvelope envelope = new ReferencedEnvelope(0, pixels.length, 0, pixels[0].length, crs);
		
		GridCoverage2D coverage = new GridCoverageFactory().create(rasterfile.getName(), pixels, envelope);
		GeoTiffWriter writer = new GeoTiffWriter(rasterfile);
		writer.write(coverage, null);
		return new GeotiffFile(rasterfile);
	}
	
	/**
	 * Create a shapefile from a file
	 * 
	 * @return
	 * @throws IOException, InvalidFileTypeException 
	 */
	public ShapeFile getShapeFile(File shapefile) throws IOException, InvalidFileTypeException {
		if(shapefile.getName().contains(SHAPEFILE_EXT))
			return new ShapeFile(shapefile);
		String[] pathArray = shapefile.getPath().split(File.separator);
		throw new InvalidFileTypeException(pathArray[pathArray.length-1], Arrays.asList(SHAPEFILE_EXT));
	}
	
	/**
	 * Create a shapefile based on a collection of feature 
	 * 
	 * @param shapefile
	 * @param features
	 * @return
	 * @throws IOException
	 * @throws SchemaException
	 */
	public ShapeFile getShapeFile(File shapefile, Collection<GSFeature> features) throws IOException, SchemaException {
		if(features.isEmpty())
			throw new IllegalStateException("GSFeature collection ("+Arrays.toString(features.toArray())+") in methode createShapeFile cannot be empty");
		ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
		
		Map<String, Serializable> params = new HashMap<>();
        params.put("url", shapefile.toURI().toURL());
        params.put("create spatial index", Boolean.TRUE);

        ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
        
        Set<SimpleFeatureType> featTypeSet = features
        		.parallelStream().map(feat -> (SimpleFeatureType) feat.getType()).collect(Collectors.toSet());
        if(featTypeSet.size() > 1)
        	throw new SchemaException("Multiple feature type to instantiate schema:\n"+Arrays.toString(featTypeSet.toArray()));
        SimpleFeatureType featureType = featTypeSet.iterator().next();
        newDataStore.createSchema(featureType);
        
        Transaction transaction = new DefaultTransaction("create");
        SimpleFeatureStore featureStore = (SimpleFeatureStore) newDataStore.getFeatureSource(newDataStore.getTypeNames()[0]);
        
        SimpleFeatureCollection collection = new ListFeatureCollection(featureType, 
        		features.stream().map(f -> (SimpleFeature) f.getInnerFeature()).collect(Collectors.toList()));
        featureStore.setTransaction(transaction);
        try {
            featureStore.addFeatures(collection);
            transaction.commit();
        } catch (Exception problem) {
            problem.printStackTrace();
            transaction.rollback();
        } finally {
            transaction.close();
        }
        
		return new ShapeFile(newDataStore);
	}
		
	public List<String> getSupportedFileFormat(){
		return supportedFileFormat;
	}
	
}
