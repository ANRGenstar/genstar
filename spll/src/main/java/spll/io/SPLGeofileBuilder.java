package spll.io;

import java.awt.Color;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.media.jai.RasterFactory;

import org.apache.commons.io.FilenameUtils;
import org.geotools.coverage.Category;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.grid.io.AbstractGridCoverageWriter;
import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.factory.Hints;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.gce.arcgrid.ArcGridWriter;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.resources.i18n.Vocabulary;
import org.geotools.resources.i18n.VocabularyKeys;
import org.geotools.util.NumberRange;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import au.com.bytecode.opencsv.CSVReader;
import core.metamodel.attribute.demographic.DemographicAttribute;
import core.metamodel.attribute.geographic.GeographicAttribute;
import core.metamodel.attribute.geographic.GeographicAttributeFactory;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.entity.AGeoEntity;
import core.metamodel.io.IGSGeofile;
import core.metamodel.value.IValue;
import core.util.data.GSEnumDataType;
import core.util.stats.GSBasicStats;
import core.util.stats.GSEnumStats;
import spll.SpllPopulation;
import spll.entity.GeoEntityFactory;
import spll.entity.SpllFeature;
import spll.io.exception.InvalidGeoFormatException;

/**
 * Constructs shapefiles
 * 
 * @author Kevin Chapuis
 *
 */
public class SPLGeofileBuilder {

	// FILE EXTENSION
	public enum SPLGisFileExtension{shp, asc, tif}

	// RASTER FILE BUILD ATTRIBUTE
	private List<float[][]> bands;
	private ReferencedEnvelope envelope;
	private double noData = SPLRasterFile.DEF_NODATA.doubleValue();
	private Color[] palette = new Color[] { new Color(254,240,217), 
			new Color(253,204,138), new Color(252,141,89), new Color(227,74,51), 
			new Color(179, 0, 0)};
	private Color noDataColor = new Color(0, 0, 0, 0);

	// SHAPE FILE BUILD ATTRIBUTE 
	private SpllPopulation population;
	private Collection<SpllFeature> features;
	private Charset charset = null;
	
	// UNSPECIFIC BUILD
	private File gisFile;

	// ----------------------- BUILD ----------------------- //

	/**
	 * Add the GIS file to be build
	 * @param gisFile
	 * @return
	 * @throws InvalidGeoFormatException
	 * @throws FileNotFoundException 
	 */
	public SPLGeofileBuilder setFile(File gisFile) 
			throws InvalidGeoFormatException, FileNotFoundException {
		chekFile();
		this.gisFile = gisFile;
		return this;
	}

	// ----------------------- RASTER ----------------------- //
	
	/**
	 * Add a specified palette
	 * @param palette
	 * @return
	 */
	public SPLGeofileBuilder setPalette(Color[] palette) {
		this.palette = palette;
		return this;
	}
	
	/**
	 * Define the noData value for rasterfile
	 * @param noData
	 * @return
	 */
	public SPLGeofileBuilder setNoData(double noData) {
		this.noData = noData;
		return this;
	}
	
	/**
	 * Define the general referenced envelope to build raster file with
	 * @param envelope
	 * @return
	 */
	public SPLGeofileBuilder setReferenceEnvelope(ReferencedEnvelope envelope) {
		this.envelope = envelope;
		return this;
	}

	/**
	 * Add bands to build raster file from
	 */
	public SPLGeofileBuilder setRasterBands(float[][]... bands) {
		int[] xy = null;
		for(float[][] band : bands) {
			if(xy == null) {
				xy = new int[2];
				xy[0] = band.length;
				xy[1] = band[0].length;
			} else {
				if(xy[0] != band.length
						|| xy[1] != band[0].length)
					throw new IllegalArgumentException("All bands should have same width x height: \n"
							+ IntStream.range(0,bands.length).mapToObj(i -> "b"+i+" ["+bands[i].length+"x"+bands[i][0].length+"]")
							.collect(Collectors.joining(" / ")));
			}
		}
		this.bands = new ArrayList<>(Arrays.asList(bands));
		return this;
	}
	
	// ----------------------- VECTOR ----------------------- //

	/**
	 * Add a localized population to build shapefile from
	 * @param population
	 * @return
	 */
	public SPLGeofileBuilder setPopulation(SpllPopulation population) {
		this.population = population;
		return this;
	}

	/**
	 * Add a collection of feature ({@link SpllFeature}) to build shapefile from
	 * @param features
	 * @return
	 */
	public SPLGeofileBuilder setFeatures(Collection<SpllFeature> features) {
		this.features = features;
		return this;
	}

	/**
	 * Add new feature to build shapefile with
	 * <p>
	 * because {@link Feature} are immutable object, we must erase former {@link SpllFeature}
	 * with new one that encapsulate updated {@link SimpleFeature} build from former one plus
	 * {@link GeographicAttribute} created from information from the csv
	 * 
	 * @param csvFile
	 * @param seperator
	 * @param keyAttribute
	 * @param keyCSV
	 * @param newAttributes
	 * @return
	 * @throws IOException 
	 */
	public SPLGeofileBuilder addAttributeToFeature(File csvFile, char seperator, String keyAttribute, String keyCSV, 
			Map<String, GSEnumDataType> newAttributes) throws IOException {
		if (features== null && features.isEmpty()) 
			throw new IllegalStateException(this.getClass().getCanonicalName()
					+" is not able to add attribute if not any feature have been added before");
		if (!csvFile.exists()) 
			throw new FileNotFoundException("File "+csvFile+" cannot be resolve to a valid file");

		CSVReader reader = null;
		try {
			reader = new CSVReader(new FileReader(csvFile), seperator);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		List<String[]> dataTable = reader.readAll();
		reader.close();
		Map<String, Map<String, String>> values = new Hashtable<String, Map<String, String>>();
		List<String> header = Arrays.asList(dataTable.get(0));

		if(!header.contains(keyCSV)) 
			throw new IllegalArgumentException("The given key to retrieve attribute from csv is not related to any csv column");
		int keyCSVIndex = header.indexOf(keyCSV);

		Map<String, Integer> attIndex = newAttributes.keySet().stream().filter(att -> header.contains(att))
				.collect(Collectors.toMap(s -> s, s -> header.indexOf(s)));
		Map<String, GeographicAttribute<? extends IValue>> attAGeo = new Hashtable<>();
		
		for (String attName : attIndex.keySet())
			attAGeo.put(attName, GeographicAttributeFactory.getFactory()
					.createAttribute(attName, newAttributes.get(attName)));

		for (String[] line : dataTable) {
			String id = line[keyCSVIndex];
			Map<String, String> vals = new Hashtable<String, String>();
			for (String name : attIndex.keySet()) {
				int idat = attIndex.get(name);
				vals.put(name, line[idat]);
			}
			values.put(id, vals);
		}
		
		//Create the new type using the former as a template
        Set<FeatureType> fTypes = features.stream()
        		.map(feat -> feat.getInnerFeature().getType()).collect(Collectors.toSet());
        if(fTypes.size() > 1)
        		throw new IllegalStateException("There is more than one feature type in feature collection"); 
        SimpleFeatureTypeBuilder stb = new SimpleFeatureTypeBuilder(); 
        stb.init((SimpleFeatureType) fTypes.iterator().next()); 
        stb.setName("augmented feature type"); 
        // add new attributes
        newAttributes.keySet().stream().forEach(attName -> stb.add(attName, IValue.class));
        GeoEntityFactory gef = new GeoEntityFactory(new HashSet<>(attAGeo.values()), stb.buildFeatureType());
		
        Set<SpllFeature> newSpllFeatures = new HashSet<>();
		for (SpllFeature ft : features) {
			Collection<String> properties = ft.getPropertiesAttribute();
			if (!properties.contains(keyAttribute)) continue;
			String objid = ft.getValueForAttribute(keyAttribute).getStringValue();

			Map<String, String> vals = values.get(objid);
			if (vals == null) continue;
			
			// New Spll feature attribute setup
			Map<GeographicAttribute<? extends IValue>, IValue> newValues = new HashMap<>(ft.getAttributeMap());
			newValues.putAll(vals.keySet().stream().collect(Collectors.toMap(vn -> attAGeo.get(vn), 
					vn -> attAGeo.get(vn).getValueSpace().addValue(vals.get(vn)))));
			newSpllFeatures.add(gef.createGeoEntity(ft.getGeometry(), newValues));
		}
		this.features = newSpllFeatures;
		
		return this;
	}

	// ------------------------------------------------------------ //
	//						CREATE GEOFILE						   //
	// ------------------------------------------------------------ //

	/**
	 * Create a geo referenced file 
	 * 
	 * @param geofile
	 * @return
	 * @throws IllegalArgumentException
	 * @throws TransformException
	 * @throws IOException
	 * @throws InvalidGeoFormatException
	 */
	public IGSGeofile<? extends AGeoEntity<? extends IValue>, ? extends IValue> buildGeofile() 
			throws IOException, IllegalArgumentException, TransformException, InvalidGeoFormatException {
		if(FilenameUtils.getExtension(this.gisFile.getName()).equals(SPLGisFileExtension.shp.toString()))
			return new SPLVectorFile(this.gisFile, this.charset);
		if(FilenameUtils.getExtension(this.gisFile.getName()).equals(SPLGisFileExtension.asc.toString())
				|| FilenameUtils.getExtension(this.gisFile.getName()).equals(SPLGisFileExtension.tif.toString()))
			return new SPLRasterFile(this.gisFile);
		chekFile(SPLGisFileExtension.values());
		throw new RuntimeException("GIS file "+gisFile+" cannot be init."); 
	}	

	/**
	 * TODO: test
	 * TODO: change float pixel value type to double (possible through JAI)
	 * 
	 * Build a raster file from a list of pixel band.
	 * 
	 * @param rasterfile
	 * @param pixels
	 * @param crs
	 * @return
	 * @throws IOException
	 * @throws IllegalArgumentException
	 * @throws TransformException
	 */
	public SPLRasterFile buildRasterfile() 
			throws IOException, IllegalArgumentException, TransformException {

		if(bands.isEmpty())
			throw new IllegalStateException(this.getClass().getCanonicalName()+" should contain bands data to build raster file");

		List<GSBasicStats<Double>> stats = bands.stream()
				.map(pix -> new GSBasicStats<Double>(GSBasicStats.transpose(pix), Arrays.asList(this.noData*1d)))
				.collect(Collectors.toList());
		float min = (float) stats.stream().mapToDouble(stat -> stat.getStat(GSEnumStats.min)[0]).min().getAsDouble();
		float max = (float) stats.stream().mapToDouble(stat -> stat.getStat(GSEnumStats.max)[0]).min().getAsDouble();

		Category nan = new Category(Vocabulary.formatInternational(VocabularyKeys.NODATA), 
				new Color[] { noDataColor },
				NumberRange.create(this.noData, this.noData));
		Category values = new Category("values", palette, 
				NumberRange.create(min, max));

		GridSampleDimension[] bandsDimension = new GridSampleDimension[] { 
				new GridSampleDimension("Dimension", new Category[] { nan, values }, null)}; 

		WritableRaster raster = RasterFactory.createBandedRaster(DataBuffer.TYPE_FLOAT,
				this.bands.get(0).length, this.bands.get(0)[0].length, this.bands.size(), null);
		int bandNb = -1;
		for(float[][] pixels : this.bands){
			bandNb++;
			for (int y=0; y<pixels[0].length; y++) 
				for (int x=0; x<pixels.length; x++) 
					raster.setSample(x, y, bandNb, pixels[x][y]);
		}

		return writeRasterFile(this.gisFile, 
				new GridCoverageFactory().create(this.gisFile.getName(), raster, this.envelope, bandsDimension));
	}

	/**
	 * 
	 * @return
	 * @throws IOException 
	 * @throws SchemaException 
	 */
	public SPLVectorFile buildShapeFile() throws IOException, SchemaException {
		if(!chekAndRenameFile(SPLGisFileExtension.shp))
			throw new RuntimeException("Not able to build requested "+this.gisFile+" shape file");
		if(population == null && features == null || features.isEmpty())
			throw new IllegalStateException("To build shape file you must first setup a sources, either features or population");

		Map<String,Serializable> key2m = new HashMap<>();
		key2m.put("url", gisFile.toURI().toURL());
		key2m.put("create spatial index", Boolean.TRUE);
		
		DataStore newDataStore = new ShapefileDataStoreFactory().createNewDataStore(key2m);
		// Three cases:
		// 1 - write down a population to a file
		if(this.population != null){
			// 1.1 - with no more feature to add to the file
			if(this.features == null || this.features.isEmpty()) {
				this.features = Collections.emptyList();
				Map<ADemoEntity, Geometry> geoms = population.getSpllPopulation()
						.stream().filter(e -> e.getLocation() != null)
						.collect(Collectors.toMap(e -> e, e ->  e.getLocation()));
				final StringBuilder specs = new StringBuilder(population.size() * 20);
				specs.append("geometry:" + getGeometryType(geoms.values()));
				List<String> atts = new ArrayList<>();
				for (final DemographicAttribute<? extends IValue> at : population.getPopulationAttributes()) {
					atts.add(at.getAttributeName());
					String name = at.getAttributeName().replaceAll("\"", "").replaceAll("'", "");
					specs.append(',').append(name).append(':').append("String");
				}
				
				newDataStore.createSchema(DataUtilities.createType("Pop", specs.toString()));
				FeatureWriter<SimpleFeatureType, SimpleFeature> fw = newDataStore
						.getFeatureWriter(newDataStore.getTypeNames()[0], Transaction.AUTO_COMMIT);
				
				for (final ADemoEntity entity : population) {
					((SimpleFeature) fw.next()).setAttributes(
							Stream.concat(Stream.of(geoms.get(entity)),
							entity.getValues().stream()).collect(Collectors.toList()));
					fw.write();
				}
				
				try {
					FileWriter fwz = new FileWriter(this.gisFile.getAbsolutePath().replace(".shp", ".prj"));
					fwz.write(population.getCrs().toString());
					fwz.close();
				} catch (final IOException e) {
					e.printStackTrace();
				}
				// 1.2 - with extended feature to add to spatial entities
			} else { 
				// TODO
			}
		}
		// 2 - write down features to a vector style file if C1. features are available C2 no population are available
		else if(this.features != null && !this.features.isEmpty()) {
			Set<FeatureType> featTypeSet = this.features.stream()
					.map(feat -> feat.getInnerFeature().getType()).collect(Collectors.toSet());
			if(featTypeSet.size() > 1)
				throw new SchemaException("Multiple feature type to instantiate schema:\n"+Arrays.toString(featTypeSet.toArray()));
			SimpleFeatureType featureType = (SimpleFeatureType) featTypeSet.iterator().next();
			newDataStore.createSchema(featureType);

			Transaction transaction = new DefaultTransaction("create");
			SimpleFeatureStore featureStore = (SimpleFeatureStore) newDataStore.getFeatureSource(newDataStore.getTypeNames()[0]);

			SimpleFeatureCollection collection = new ListFeatureCollection(featureType, 
					this.features.stream().map(f -> (SimpleFeature) f.getInnerFeature()).collect(Collectors.toList()));
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
		}

		return new SPLVectorFile(newDataStore, new HashSet<>(this.features));
	}

	// -------------------------------------------------------------------------- //
	// ------------------- FACTORY STYLE STATIC GIS FILE INIT ------------------- //
	// -------------------------------------------------------------------------- //

	/**
	 * Create a shapefile from a file
	 * 
	 * @param shapefile the file to parse
	 * @param charset the charset to use for parsing the shapefile (null for default)
	 * @return
	 * @throws IOException, InvalidFileTypeException 
	 */
	public static SPLVectorFile getShapeFile(File shapefile, Charset charset) throws IOException, InvalidGeoFormatException {
		if(FilenameUtils.getExtension(shapefile.getName()).equals(SPLGisFileExtension.shp.toString()))
			return new SPLVectorFile(shapefile, charset);
		String[] pathArray = shapefile.getPath().split(File.separator);
		throw new InvalidGeoFormatException(pathArray[pathArray.length-1], Arrays.asList(SPLGisFileExtension.shp));
	}

	/**
	 * TODO: javadoc
	 * 
	 * @param shapefile
	 * @param attributes
	 * @param charset charset to use to decode the file (null for default)
	 * @return
	 * @throws IOException
	 * @throws InvalidGeoFormatException
	 */
	public static SPLVectorFile getShapeFile(File shapefile, List<String> attributes, Charset charset) 
						throws IOException, InvalidGeoFormatException {
		if(FilenameUtils.getExtension(shapefile.getName()).equals(SPLGisFileExtension.shp.toString()))
			return new SPLVectorFile(shapefile, charset, attributes);
		String[] pathArray = shapefile.getPath().split(File.separator);
		throw new InvalidGeoFormatException(pathArray[pathArray.length-1], Arrays.asList(SPLGisFileExtension.shp));
	}


	// ------------------------------------------------------- //
	// ------------------- INNER UTILITIES ------------------- //
	// ------------------------------------------------------- //

	/*
	 * Write down either asc or tif file
	 */
	private SPLRasterFile writeRasterFile(File rasterfile, GridCoverage2D coverage) 
			throws IllegalArgumentException, TransformException, IOException {
		AbstractGridCoverageWriter writer;
		try {
			chekFile(SPLGisFileExtension.asc, SPLGisFileExtension.tif);
		} catch (FileNotFoundException e) {
			this.gisFile.createNewFile();
		} catch (InvalidGeoFormatException e) {
			e.printStackTrace();
			System.exit(1);
		}
		if(FilenameUtils.getExtension(rasterfile.getName()).equals(SPLGisFileExtension.asc.toString()))
			writer = new ArcGridWriter(rasterfile, new Hints(Hints.USE_JAI_IMAGEREAD, true));
		else
			writer = new GeoTiffWriter(rasterfile);
		writer.write(coverage, null);
		return new SPLRasterFile(rasterfile);
	}

	/*
	 * retrieve the string formated type for this geometry
	 */
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

	/*
	 * Check if file exists and is consistent with expected extension
	 */
	private void chekFile(SPLGisFileExtension... expectedExtension) throws FileNotFoundException, InvalidGeoFormatException {
		if(this.gisFile == null || !Files.exists(gisFile.toPath()))
			throw new FileNotFoundException("GIS file "+gisFile+" is unidentified");
		if(Stream.of(expectedExtension).noneMatch(ext -> 
		FilenameUtils.getExtension(this.gisFile.getName()).equals(ext.toString()))){
			String[] pathArray = gisFile.getPath().split(File.separator);
			throw new InvalidGeoFormatException(pathArray[pathArray.length-1], Arrays.asList(SPLGisFileExtension.values()));
		}
	}

	/*
	 * Check if file exists and insure extension compliance
	 */
	private boolean chekAndRenameFile(SPLGisFileExtension expectedExtension) throws IOException {
		try {
			chekFile(SPLGisFileExtension.shp);
		} catch (FileNotFoundException e) {
			return this.gisFile.createNewFile();
		} catch (InvalidGeoFormatException e) {
			return gisFile.renameTo(new File(FilenameUtils
					.removeExtension(gisFile.getAbsolutePath())+"."+SPLGisFileExtension.shp));
		}
		return false;
	}
}
