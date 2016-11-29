package core.io.geo;

import java.awt.Color;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
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

import javax.media.jai.RasterFactory;

import org.apache.commons.io.FilenameUtils;
import org.geotools.coverage.Category;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.grid.io.AbstractGridCoverageWriter;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.factory.Hints;
import org.geotools.feature.SchemaException;
import org.geotools.gce.arcgrid.ArcGridWriter;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.resources.i18n.Vocabulary;
import org.geotools.resources.i18n.VocabularyKeys;
import org.geotools.util.NumberRange;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import core.io.exception.InvalidFileTypeException;
import core.io.geo.entity.GSFeature;
import core.util.GSBasicStats;
import core.util.data.GSEnumStats;

public class GeofileFactory {

	public  static String SHAPEFILE_EXT = "shp";
	public static String ARC_EXT = "asc";
	public static String GEOTIFF_EXT = "tif";
	
	public static List<String> getSupportedFileFormat(){
		return Arrays.asList(SHAPEFILE_EXT, GEOTIFF_EXT, ARC_EXT);
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
		if(FilenameUtils.getExtension(geofile.getName()).equals(SHAPEFILE_EXT))
			return new ShapeFile(geofile);
		if(FilenameUtils.getExtension(geofile.getName()).equals(GEOTIFF_EXT) || 
				FilenameUtils.getExtension(geofile.getName()).equals(ARC_EXT))
			return new RasterFile(geofile);
		String[] pathArray = geofile.getPath().split(File.separator);
		throw new InvalidFileTypeException(pathArray[pathArray.length-1], Arrays.asList(SHAPEFILE_EXT, GEOTIFF_EXT, ARC_EXT));
	}
	
	/**
	 * Create a shapefile from a file
	 * 
	 * @return
	 * @throws IOException, InvalidFileTypeException 
	 */
	public ShapeFile getShapeFile(File shapefile) throws IOException, InvalidFileTypeException {
		if(FilenameUtils.getExtension(shapefile.getName()).equals(SHAPEFILE_EXT))
			return new ShapeFile(shapefile);
		String[] pathArray = shapefile.getPath().split(File.separator);
		throw new InvalidFileTypeException(pathArray[pathArray.length-1], Arrays.asList(SHAPEFILE_EXT));
	}
	
	
	// ------------------------------------------------------------ //
	//						CREATE GEOFILE							//
	// ------------------------------------------------------------ //
	

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
	public RasterFile createRasterfile(File rasterfile, float[][] pixels, CoordinateReferenceSystem crs) 
			throws IOException, IllegalArgumentException, TransformException {
		GridCoverage2D coverage = new GridCoverageFactory().create(rasterfile.getName(), pixels, 
				new ReferencedEnvelope(0, pixels.length, 0, pixels[0].length, crs));
		return writeRasterFile(rasterfile, coverage);
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
	public RasterFile createRasterfile(File rasterfile, float[][] pixels, float noData, CoordinateReferenceSystem crs) 
			throws IOException, IllegalArgumentException, TransformException {
		// Create image options based on pixels' characteristics
		ReferencedEnvelope envelope = new ReferencedEnvelope(0, pixels.length, 0, pixels[0].length, crs);

		GSBasicStats<Double> gsbs = new GSBasicStats<Double>(GSBasicStats.transpose(pixels), Arrays.asList(new Double(noData)));

		Category nan = new Category(Vocabulary.formatInternational(VocabularyKeys.NODATA), 
				new Color[] { new Color(0, 0, 0, 0) },
				NumberRange.create(noData, noData));
		Category values = new Category("values", 
				new Color[] { Color.BLUE, Color.CYAN, Color.GREEN, Color.YELLOW, Color.RED }, 
				NumberRange.create(gsbs.getStat(GSEnumStats.min)[0], gsbs.getStat(GSEnumStats.max)[0]));

		GridSampleDimension[] bands = new GridSampleDimension[] { 
				new GridSampleDimension("Dimension", new Category[] { nan, values }, null)}; 

		WritableRaster raster = RasterFactory.createBandedRaster(DataBuffer.TYPE_FLOAT,
				pixels.length, pixels[0].length, 1, null);
		for (int y=0; y<pixels[0].length; y++) {
			for (int x=0; x<pixels.length; x++) {
				raster.setSample(x, y, 0, pixels[x][y]);
			}
		}
		
		return writeRasterFile(rasterfile, 
				new GridCoverageFactory().create(rasterfile.getName(), raster, envelope, bands));
	}

	/**
	 * TODO: proper implementation
	 * 
	 * @param rasterfile
	 * @param crs
	 * @param noData
	 * @param multiBandMatrix
	 * @return
	 * @throws IOException
	 * @throws IllegalArgumentException
	 * @throws TransformException
	 */
	public RasterFile createRasterFile(File rasterfile, GridSampleDimension[] bands, CoordinateReferenceSystem crs, double[][]... multiBandMatrix) 
			throws IOException, IllegalArgumentException, TransformException {
		// Create image options based on pixels' characteristics
		ReferencedEnvelope envelope = new ReferencedEnvelope(0, multiBandMatrix[0].length, 0, multiBandMatrix[0][0].length, crs);

		SampleModel sample = RasterFactory.createBandedSampleModel(DataBuffer.TYPE_DOUBLE, (int) envelope.getWidth(), (int) envelope.getHeight(), multiBandMatrix.length);
		WritableRaster raster = RasterFactory.createWritableRaster(sample, null);

		for(int b = 0; b < multiBandMatrix.length; b++){
			for(int x = 0; x < multiBandMatrix[b].length; x++){
				for(int y = 0; y < multiBandMatrix[b][x].length; y++)
					raster.setSample(x, y, b, multiBandMatrix[b][y][x]);
			}
		}

		return writeRasterFile(rasterfile, 
				new GridCoverageFactory().create(rasterfile.getName(), raster, envelope, bands));
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
	public ShapeFile createShapeFile(File shapefile, Collection<GSFeature> features) throws IOException, SchemaException {
		if(features.isEmpty())
			throw new IllegalStateException("GSFeature collection ("+Arrays.toString(features.toArray())+") in methode createShapeFile cannot be empty");
		ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

		Map<String, Serializable> params = new HashMap<>();
		params.put("url", shapefile.toURI().toURL());
		params.put("create spatial index", Boolean.TRUE);

		ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);

		Set<SimpleFeatureType> featTypeSet = features
				.parallelStream().map(feat -> (SimpleFeatureType) feat.getInnerFeature().getType()).collect(Collectors.toSet());
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
	
	// ------------------- INNER UTILITIES ------------------- //
	

	private RasterFile writeRasterFile(File rasterfile, GridCoverage2D coverage) 
			throws IllegalArgumentException, TransformException, IOException {
		AbstractGridCoverageWriter writer;
		if(FilenameUtils.getExtension(rasterfile.getName()).contains(ARC_EXT))
			writer = new ArcGridWriter(rasterfile, new Hints(Hints.USE_JAI_IMAGEREAD, true));
		else
			writer = new GeoTiffWriter(rasterfile);
		writer.write(coverage, null);
		return new RasterFile(rasterfile);
	}

}
