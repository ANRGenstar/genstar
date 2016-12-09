package spll.io;

import java.awt.Color;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
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
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureWriter;
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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import core.metamodel.IPopulation;
import core.metamodel.geo.AGeoEntity;
import core.metamodel.geo.io.IGSGeofile;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;
import core.util.stats.GSBasicStats;
import core.util.stats.GSEnumStats;
import spll.entity.GSFeature;
import spll.io.exception.InvalidGeoFormatException;

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
	 * @throws InvalidGeoFormatException
	 */
	public IGSGeofile<? extends AGeoEntity> getGeofile(File geofile) 
			throws IllegalArgumentException, TransformException, IOException, InvalidGeoFormatException{
		if(FilenameUtils.getExtension(geofile.getName()).equals(SHAPEFILE_EXT))
			return new ShapeFile(geofile);
		if(FilenameUtils.getExtension(geofile.getName()).equals(GEOTIFF_EXT) || 
				FilenameUtils.getExtension(geofile.getName()).equals(ARC_EXT))
			return new RasterFile(geofile);
		String[] pathArray = geofile.getPath().split(File.separator);
		throw new InvalidGeoFormatException(pathArray[pathArray.length-1], Arrays.asList(SHAPEFILE_EXT, GEOTIFF_EXT, ARC_EXT));
	}
	
	/**
	 * Create a shapefile from a file
	 * 
	 * @return
	 * @throws IOException, InvalidFileTypeException 
	 */
	public ShapeFile getShapeFile(File shapefile) throws IOException, InvalidGeoFormatException {
		if(FilenameUtils.getExtension(shapefile.getName()).equals(SHAPEFILE_EXT))
			return new ShapeFile(shapefile);
		String[] pathArray = shapefile.getPath().split(File.separator);
		throw new InvalidGeoFormatException(pathArray[pathArray.length-1], Arrays.asList(SHAPEFILE_EXT));
	}
	
	public ShapeFile getShapeFile(File shapefile,List<String> attributes) throws IOException, InvalidGeoFormatException {
		if(FilenameUtils.getExtension(shapefile.getName()).equals(SHAPEFILE_EXT))
			return new ShapeFile(shapefile, attributes);
		String[] pathArray = shapefile.getPath().split(File.separator);
		throw new InvalidGeoFormatException(pathArray[pathArray.length-1], Arrays.asList(SHAPEFILE_EXT));
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
	public RasterFile createRasterfile(File rasterfile, float[][] pixels, float noData, 
			ReferencedEnvelope envelope) 
			throws IOException, IllegalArgumentException, TransformException {
		// Create image options based on pixels' characteristics
		
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
	public RasterFile createRasterFile(File rasterfile, GridSampleDimension[] bands, 
			CoordinateReferenceSystem crs, double[][]... multiBandMatrix) 
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
	 * Export a population in a shapefile
	 * 
	 * TODO: explain more
	 * 
	 * @param shapefile
	 * @param population
	 * @param crs
	 * @return
	 * @throws IOException
	 * @throws SchemaException
	 */
	public ShapeFile createShapeFile(File shapefile, 
			IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> population, 
			CoordinateReferenceSystem crs) throws IOException, SchemaException {
		if(population.isEmpty()) 
			throw new IllegalStateException("Population ("+Arrays.toString(population.toArray())+") in methode createShapeFile cannot be empty");
		ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

		Map<String, Serializable> params = new HashMap<>();
		params.put("url", shapefile.toURI().toURL());
		params.put("create spatial index", Boolean.TRUE);

		ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);

		Map<APopulationEntity, Geometry> geoms = population.stream().filter(e -> e.getLocation() != null)
				.collect(Collectors.toMap(e -> e, e -> e.getNest().getGeometry()));

		final StringBuilder specs = new StringBuilder(population.size() * 20);
		final String geomType = getGeometryType(geoms.values());

		specs.append("geometry:" + geomType);
		List<String> atts = new ArrayList<>();
			for (final APopulationAttribute at : population.getPopulationAttributes()) {
				atts.add(at.getAttributeName());
				String name = at.getAttributeName().replaceAll("\"", "");
				name = name.replaceAll("'", "");
				final String type = "String";
				specs.append(',').append(name).append(':').append(type);
			}
		final SimpleFeatureType type = DataUtilities.createType(newDataStore.getFeatureSource().getEntry().getTypeName(),
					specs.toString());
		
		newDataStore.createSchema(type);
	

		try (@SuppressWarnings("rawtypes")
		FeatureWriter fw = newDataStore.getFeatureWriter(Transaction.AUTO_COMMIT)) {

			final List<Object> values = new ArrayList<>();

			for (final APopulationEntity entity : population) {
				values.clear();
				final SimpleFeature ff = (SimpleFeature) fw.next();
				values.add(geoms.get(entity));
				for (final String att : atts) {
					values.add(entity.getValueForAttribute(att));
				}
				ff.setAttributes(values);
				fw.write();
			}
			// store.dispose();
			if (crs != null) {
				try (FileWriter fwz = new FileWriter(shapefile.getAbsolutePath().replace(".shp", ".prj"))) {
					fwz.write(crs.toString());
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		} 	
		

		return new ShapeFile(newDataStore, null);
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

		return new ShapeFile(newDataStore, null);
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

	private String getGeometryType(final Collection<Geometry> geoms) {
		String geomType = "";
		for (final Geometry geom : geoms) {
			if (geom != null) {
				geomType = geom.getClass().getSimpleName();
				if (geom.getNumGeometries() > 1) {
					if (geom.getGeometryN(0).getClass() == Point.class) {
						geomType = MultiPoint.class.getSimpleName();
					} else if (geom.getGeometryN(0).getClass() == LineString.class) {
						geomType = MultiLineString.class.getSimpleName();
					} else if (geom.getGeometryN(0).getClass() == Polygon.class) {
						geomType = MultiPolygon.class.getSimpleName();
					}
					break;
				}
			}
		}

		if ("DynamicLineString".equals(geomType))
			geomType = LineString.class.getSimpleName();
		return geomType;
	}
}
