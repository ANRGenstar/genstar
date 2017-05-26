package spll.io;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
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
import org.geotools.factory.Hints;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Geometry;

import core.metamodel.geo.AGeoAttribute;
import core.metamodel.geo.AGeoEntity;
import core.metamodel.geo.AGeoValue;
import core.metamodel.geo.io.GeoGSFileType;
import core.metamodel.geo.io.IGSGeofile;
import spll.entity.GSPixel;
import spll.entity.GeoEntityFactory;
import spll.entity.iterator.GSPixelIterator;
import spll.util.SpllUtil;

/**
 * 
 * File that represent generic raster data. 
 * 
 * <p>
 * Available input format can be found at
 * {@link SPLGeofileFactory#getSupportedFileFormat()} 
 * 
 * @author kevinchapuis
 *
 */
public class SPLRasterFile implements IGSGeofile<GSPixel> {

	private final GridCoverage2D coverage;
	private final AbstractGridCoverage2DReader store;
	
	private final GeoEntityFactory gef;
	
	public static Number DEF_NODATA = -9999; 
	private Number noData;

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
	public SPLRasterFile(File file) throws TransformException, IllegalArgumentException, IOException {
		ParameterValue<OverviewPolicy> policy = AbstractGridFormat.OVERVIEW_POLICY.createValue();
		policy.setValue(OverviewPolicy.IGNORE);

		//this will basically read 4 tiles worth of data at once from the disk...
		ParameterValue<String> gridsize = AbstractGridFormat.SUGGESTED_TILE_SIZE.createValue();

		//Setting read type: use JAI ImageRead (true) or ImageReaders read methods (false)
		ParameterValue<Boolean> useJaiRead = AbstractGridFormat.USE_JAI_IMAGEREAD.createValue();
		useJaiRead.setValue(true);

		// TODO: fill in the factory with all possible attribute for this file
		this.gef = new GeoEntityFactory(new HashSet<>());

		if(FilenameUtils.getExtension(file.getName()).equals(SPLGeofileFactory.ARC_EXT)){
			this.store = GridFormatFinder.findFormat(file).getReader(file);
		} else if(FilenameUtils.getExtension(file.getName()).equals(SPLGeofileFactory.GEOTIFF_EXT)){
			this.store = new GeoTiffReader(file, new Hints(Hints.USE_JAI_IMAGEREAD, true));
			this.noData = ((GeoTiffReader) store).getMetadata().getNoData();
		} else
			throw new IOException("File format "+FilenameUtils.getExtension(file.getName())+" is not supported "
					+ "\nSupported file type are: "+Arrays.toString(SPLGeofileFactory.getSupportedFileFormat().toArray()));
		 
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
	public boolean isCoordinateCompliant(IGSGeofile<? extends AGeoEntity> file) {
		CoordinateReferenceSystem thisCRS = null, fileCRS = null;
		thisCRS = SpllUtil.getCRSfromWKT(this.getWKTCoordinateReferentSystem());
		fileCRS = SpllUtil.getCRSfromWKT(file.getWKTCoordinateReferentSystem());
		return thisCRS == null && fileCRS == null ? false : thisCRS.equals(fileCRS);
	}

	@Override
	public String getWKTCoordinateReferentSystem() {
		return coverage.getCoordinateReferenceSystem().toWKT();
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
	public Collection<GSPixel> getGeoEntity(){
		Set<GSPixel> collection = new HashSet<>(); 
		getGeoEntityIterator().forEachRemaining(collection::add);
		return collection;
	}
	
	@Override
	public Collection<AGeoValue> getGeoValues() {
		Set<AGeoValue> values = new HashSet<>();
		getGeoEntityIterator().forEachRemaining(pix -> values.addAll(pix.getValues()));
		return values;
	}
	
	@Override
	public Collection<AGeoAttribute> getGeoAttributes(){
		return getGeoEntity().stream().flatMap(entity -> entity.getAttributes().stream())
				.collect(Collectors.toSet());
	}
	
	// ------------------------------------- //
	
	@Override
	public Collection<GSPixel> getGeoEntityWithin(Geometry geom) {
		Set<GSPixel> collection = new HashSet<>(); 
		getGeoEntityIteratorWithin(geom).forEachRemaining(collection::add);
		return collection;
	}
	
	@Override
	public Collection<GSPixel> getGeoEntityIntersect(Geometry geom) {
		Set<GSPixel> collection = new HashSet<>(); 
		getGeoEntityIteratorIntersect(geom).forEachRemaining(collection::add);
		return collection;
	}
	
	@Override
	public Iterator<GSPixel> getGeoEntityIterator() {
		return new GSPixelIterator(store.getGridCoverageCount(), coverage);
	}

	@Override
	public Iterator<GSPixel> getGeoEntityIteratorWithin(Geometry geom) {
		Crop cropper = new Crop(); 
		ParameterValueGroup param = cropper.getParameters();
		param.parameter("Source").setValue(coverage); // Nul nul nul et si jamais il change le nom du parametre ???
		param.parameter(Crop.PARAMNAME_ROI).setValue(geom);
		GridCoverage2D newCoverage = (GridCoverage2D) cropper.doOperation(param, null);
		return new GSPixelIterator(store.getGridCoverageCount(), newCoverage);
	}
	
	@Override 
	public Iterator<GSPixel> getGeoEntityIteratorIntersect(Geometry geom) {
		return getGeoEntityIteratorWithin(geom);
	}
	
	// ------------------- specific geotiff accessors ------------------- //
	
	public AbstractGridCoverage2DReader getStore() {
		return store;
	}
	
	public double getNoDataValue() {
		return noData.doubleValue();
	}
	
	public boolean isNoDataValue(AGeoValue var) {
		return var.getNumericalValue().equals(noData);
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
	public GSPixel getPixel(int x, int y) throws TransformException {
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
		String bandName = GeoEntityFactory.ATTRIBUTE_PIXEL_BAND+i;
		float[][] matrix = new float[getColumnNumber()][getRowNumber()];
		for (int row = 0; row < getRowNumber(); row++)
			for (int col = 0; col < getColumnNumber(); col++)
				matrix[col][row] = this.getPixel(col, row).getValueForAttribute(bandName)
						.getNumericalValue().floatValue();
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
	
	@Override
	public String toString(){
		String s = "";
		for(String key : store.getMetadataNames()){
			s += key+": "+store.getMetadataValue(key)+"\n";
		}
		return s;
	}

}
