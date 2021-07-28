package spll.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.geotools.coverage.grid.GridCoordinates2D;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridEnvelope2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.geotools.coverage.grid.io.OverviewPolicy;
import org.geotools.coverage.processing.operation.Crop;
import org.geotools.util.factory.Hints;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import org.locationtech.jts.geom.Geometry;

import core.metamodel.attribute.Attribute;
import core.metamodel.entity.AGeoEntity;
import core.metamodel.io.IGSGeofile;
import core.metamodel.value.IValue;
import core.metamodel.value.numeric.ContinuousValue;
import spll.entity.GeoEntityFactory;
import spll.entity.SpllPixel;
import spll.entity.iterator.GSPixelIterator;
import spll.io.SPLGeofileBuilder.SPLGisFileExtension;
import spll.io.exception.InvalidGeoFormatException;
import spll.util.SpllUtil;

/**
 * 
 * File that represent generic raster data. 
 * 
 * <p>
 * Available input format can be found at
 * {@link SPLGeofileBuilder#getSupportedFileFormat()} 
 * 
 * @author kevinchapuis
 *
 */
public class SPLRasterFile implements IGSGeofile<SpllPixel, ContinuousValue> {

	private final GridCoverage2D coverage;
	private final AbstractGridCoverage2DReader store;
	
	private final GeoEntityFactory gef;
	
	public static Number DEF_NODATA = -9999; 
	private Number noData;
	
	private Collection<SpllPixel> cacheGeoEntity = null;
	private Collection<ContinuousValue> cacheGeoValues = null;
	private Collection<Attribute<? extends ContinuousValue>> cacheGeoAttributes = null;

	/**
	 * 
	 * INFO: implementation partially rely on stackexchange answer below:
	 * {@link http://gis.stackexchange.com/questions/106882/how-to-read-each-pixel-of-each-band-of-a-multiband-geotiff-with-geotools-java}
	 * 
	 * @param inputPath
	 * @throws IOException
	 * @throws TransformException
	 * @throws IllegalArgumentException 
	 */
	protected SPLRasterFile(File file) throws TransformException, IllegalArgumentException, IOException {
		ParameterValue<OverviewPolicy> policy = AbstractGridFormat.OVERVIEW_POLICY.createValue();
		policy.setValue(OverviewPolicy.IGNORE);

		//this will basically read 4 tiles worth of data at once from the disk...
		ParameterValue<String> gridsize = AbstractGridFormat.SUGGESTED_TILE_SIZE.createValue();

		//Setting read type: use JAI ImageRead (true) or ImageReaders read methods (false)
		ParameterValue<Boolean> useJaiRead = AbstractGridFormat.USE_JAI_IMAGEREAD.createValue();
		useJaiRead.setValue(true);

		// TODO: fill in the factory with all possible attribute for this file
		this.gef = new GeoEntityFactory(new HashSet<>());

		if(FilenameUtils.getExtension(file.getName()).equals(SPLGisFileExtension.asc.toString())){
			this.store = GridFormatFinder.findFormat(file).getReader(file);
		} else if(FilenameUtils.getExtension(file.getName()).equals(SPLGisFileExtension.tif.toString())){
			this.store = new GeoTiffReader(file, new Hints(Hints.USE_JAI_IMAGEREAD, true));
			this.noData = ((GeoTiffReader) store).getMetadata().getNoData();
		} else
			throw new IOException("File format "+FilenameUtils.getExtension(file.getName())+" is not supported "
					+ "\nSupported file type are: "+Arrays.toString(SPLGisFileExtension.values()));
		 
		this.coverage = this.store.read(new GeneralParameterValue[]{policy, gridsize, useJaiRead});	
	}
	
	// ------------------ General contract ------------------ //
	
	@Override
	public GeoGSFileType getGeoGSFileType(){
		return GeoGSFileType.RASTER;
	}
	
	@Override
	public ReferencedEnvelope getEnvelope() {
		return new ReferencedEnvelope(coverage.getEnvelope2D());
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
		return coverage.getCoordinateReferenceSystem().toWKT();
	}
	
	@Override 
	public IGSGeofile<SpllPixel, ContinuousValue> transferTo(File destination,
			Map<? extends AGeoEntity<? extends IValue>, Number> transfer,
			Attribute<? extends IValue> attribute) 
					throws IllegalArgumentException, IOException {
		if(!attribute.getValueSpace().getType().isNumericValue())
			throw new IllegalArgumentException("Raster file cannot be template for non numeric data tranfer\n"
					+ "Trying to force attribute "+attribute.getAttributeName()+" of type "+attribute.getValueSpace().getType()
					+ " to fit a numerical type");
		
		float[][] bands = new float[this.getColumnNumber()][this.getRowNumber()]; 
		
		Iterator<SpllPixel> it = this.getGeoEntityIterator();
		while(it.hasNext()) {
			SpllPixel pix = it.next();
			bands[pix.getGridX()][pix.getGridY()] = transfer.get(pix).floatValue(); 
		}
		
		IGSGeofile<SpllPixel, ContinuousValue> res = null;
		
		try {
			res = new SPLGeofileBuilder().setRasterBands(bands).setFile(destination)
					.setReferenceEnvelope(this.getEnvelope()).buildRasterfile();
		} catch (TransformException | InvalidGeoFormatException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return res;
	}
	
	
	// ---------------------------------------------------------------- //
	// ----------------------- ACCESS TO VALUES ----------------------- //
	// ---------------------------------------------------------------- //
	
	/**
	 * {@inheritDoc}
	 * 
	 * Collection of geo data could lead to overload memory. Iterators should be use
	 * to save memory 
	 * 
	 */
	@Override
	public Collection<SpllPixel> getGeoEntity(){
		if (cacheGeoEntity == null) {
			cacheGeoEntity = new ArrayList<>(); 
			getGeoEntityIterator().forEachRemaining(cacheGeoEntity::add);
		}
		return cacheGeoEntity;
	}
	
	@Override
	public Collection<ContinuousValue> getGeoValues() {
		if (cacheGeoValues == null) {
			cacheGeoValues = new HashSet<>();
			getGeoEntityIterator().forEachRemaining(pix -> cacheGeoValues.addAll(pix.getValues()));
		}
		return cacheGeoValues;
	}
	
	@Override
	public Collection<Attribute<? extends ContinuousValue>> getGeoAttributes(){
		if (cacheGeoAttributes == null) {
			cacheGeoAttributes = getGeoEntity().stream().flatMap(entity -> entity.getAttributes().stream())
				.collect(Collectors.toSet());
		}
		return cacheGeoAttributes;
	
	}
	
	// ------------------------------------- //
	
	@Override
	public Collection<SpllPixel> getGeoEntityWithin(Geometry geom) {
		ArrayList<SpllPixel> collection = new ArrayList<>(); 
		getGeoEntityIteratorWithin(geom).forEachRemaining(collection::add);
		return collection;
	}
	
	@Override
	public Collection<SpllPixel> getGeoEntityIntersect(Geometry geom) {
		Set<SpllPixel> collection = new HashSet<>(); 
		getGeoEntityIteratorIntersect(geom).forEachRemaining(collection::add);
		return collection;
	}
	
	@Override
	public Iterator<SpllPixel> getGeoEntityIterator() {
		return new GSPixelIterator(store.getGridCoverageCount(), coverage);
	}

	@Override
	public Iterator<SpllPixel> getGeoEntityIteratorWithin(Geometry geom) {
		Crop cropper = new Crop(); 
		ParameterValueGroup param = cropper.getParameters();
		param.parameter("Source").setValue(coverage); // Nul nul nul et si jamais il change le nom du parametre ???
		param.parameter(Crop.PARAMNAME_ROI).setValue(geom);
		GridCoverage2D newCoverage = (GridCoverage2D) cropper.doOperation(param, null);
		return new GSPixelIterator(store.getGridCoverageCount(), newCoverage);
	}
	
	@Override 
	public Iterator<SpllPixel> getGeoEntityIteratorIntersect(Geometry geom) {
		return getGeoEntityIteratorWithin(geom);
	
	}
	
	// ------------------- specific geotiff accessors ------------------- //
	
	public AbstractGridCoverage2DReader getStore() {
		return store;
	}
	
	public double getNoDataValue() {
		return noData.doubleValue();
	}
	
	public String[] getBandId(){
		return store.getGridCoverageNames();
	}
	
	public int getRowNumber(){
		return store.getOriginalGridRange().getHigh(1)+1;
	}
	
	public int getColumnNumber(){
		return store.getOriginalGridRange().getHigh(0)+1;
	}
	
	/**
	 * Gives the pixel that can be found at coordinate {@code x y}
	 * in 0 based coordinate (bottom left corner)
	 * 
	 * @param x
	 * @param y
	 * @return
	 * @throws TransformException
	 */
	public SpllPixel getPixel(int x, int y) throws TransformException {
		x += coverage.getGridGeometry().getGridRange2D().x;
		y += coverage.getGridGeometry().getGridRange2D().y;
		double[] vals = new double[store.getGridCoverageCount()]; 
		coverage.evaluate(new GridCoordinates2D(x, y), vals);
		Double[] valsN = new Double[vals.length];
		for(int k = 0; k < vals.length; k++)
			valsN[k] = vals[k];
		return gef.createGeoEntity(valsN, coverage.getGridGeometry()
				.gridToWorld(new GridEnvelope2D(x, y, 1, 1)), x, y);
	}
	
	/**
	 * Gives the entire matrix of value for raster band number {@code i}
	 * 
	 * @param i
	 * @return
	 * @throws TransformException
	 */
	public float[][] getMatrix(int i) throws TransformException {
		if(coverage.getNumSampleDimensions() < i)
			throw new IllegalArgumentException("This raster data file does not have more than "
					+ coverage.getNumSampleDimensions() + " data bands while ask for band n° "+i);
		String bandName = GeoEntityFactory.ATTRIBUTE_PIXEL_BAND+i;
		float[][] matrix = new float[getColumnNumber()][getRowNumber()];
		Iterator<SpllPixel> pixIt = this.getGeoEntityIterator(); 
		while(pixIt.hasNext()) {
			SpllPixel pix = pixIt.next();
			matrix[pix.getGridX()][pix.getGridY()] = pix.getAttributes().stream()
					.filter(attribute -> attribute.getAttributeName().equals(bandName))
					.map(attribute -> pix.getNumericValueForAttribute(attribute))
					.findFirst().get().floatValue();
		}
		return matrix;
	}


	// --------------------------- Utilities --------------------------- // 
	
	public String printValues() {
		String s = "";
		int numRows = getRowNumber();
		int numCols = getColumnNumber();
		final GeneralEnvelope genv = store.getOriginalEnvelope();

		final double cellHeight = genv.getSpan(1) / numRows;
		final double cellWidth = genv.getSpan(0) / numCols;
		final double originX = genv.getMinimum(0);
		final double maxY = genv.getMaximum(1);

		final double cmx = cellWidth / 2;
		final double cmy = cellHeight / 2;

		s += "nb Rows:" + numRows + " numCols:" + numCols + "\n";
		for ( int i = 0, n = numRows * numCols; i < n; i++ ) {
			final int yy = i / numCols;
			final int xx = i - yy * numCols;

			double x = originX + xx * cellWidth + cmx;
			double y = maxY - (yy * cellHeight + cmy);

			final Object vals = coverage.evaluate(new DirectPosition2D(x,y));
			s += "vals: " + (Arrays.toString((byte[])vals))+"\n";
		}	
		return s;
	}
	
	public void clearCache(){
		if(cacheGeoEntity != null) {
			cacheGeoEntity.clear();
			cacheGeoEntity = null;
		}
		if (cacheGeoAttributes != null) {
			cacheGeoAttributes.clear();
			cacheGeoAttributes = null;
		}
		if (cacheGeoValues != null) {
			cacheGeoValues.clear();
			cacheGeoValues = null;
		}
	}
	
	@Override
	public String toString(){
		String s = "";
		if (store.getMetadataNames() != null && store.getMetadataNames().length > 0)
			for(String key : store.getMetadataNames()){
				s += key+": "+store.getMetadataValue(key)+"\n";
			}
		if (store.getGridCoverageNames() != null && store.getGridCoverageNames().length > 0)
			for(String key : store.getGridCoverageNames()){
				s += key+" ";
			}
		return s;
	}
	
}
