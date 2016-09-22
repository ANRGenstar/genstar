package io.datareaders.georeader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.DoubleStream;

import org.geotools.coverage.grid.GridCoordinates2D;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.OverviewPolicy;
import org.geotools.factory.Hints;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.GeneralEnvelope;
import org.opengis.coverage.PointOutsideCoverageException;
import org.opengis.feature.Feature;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Point;

import io.datareaders.georeader.geodat.GSPixel;

public class GeotiffFileIO implements IGeoGSFileIO<Number, Double> {

	private final AbstractGridCoverage2DReader store;
	private final GridCoverage2D coverage;
	private final String[] bandId;
	
	private List<GSPixel> pixels = new ArrayList<>();

	/**
	 * Basically convert each grid like data from tiff file to a {@link Feature} list: 
	 * pixels are change into {@link Point} with values stored as "band" array of double
	 * 
	 * INFO: implementation partially rely on stackexchange answer below:
	 * {@link http://gis.stackexchange.com/questions/106882/how-to-read-each-pixel-of-each-band-of-a-multiband-geotiff-with-geotools-java}
	 * 
	 * @param inputPath
	 * @throws IOException
	 * @throws TransformException
	 */
	public GeotiffFileIO(String inputPath) throws IOException, TransformException {
		ParameterValue<OverviewPolicy> policy = AbstractGridFormat.OVERVIEW_POLICY.createValue();
		policy.setValue(OverviewPolicy.IGNORE);

		//this will basically read 4 tiles worth of data at once from the disk...
		ParameterValue<String> gridsize = AbstractGridFormat.SUGGESTED_TILE_SIZE.createValue();

		//Setting read type: use JAI ImageRead (true) or ImageReaders read methods (false)
		ParameterValue<Boolean> useJaiRead = AbstractGridFormat.USE_JAI_IMAGEREAD.createValue();
		useJaiRead.setValue(true);

		this.store = new GeoTiffReader(new File(inputPath), new Hints(Hints.USE_JAI_IMAGEREAD, true));
		this.coverage = store.read(new GeneralParameterValue[]{policy, gridsize, useJaiRead});
		this.bandId = this.store.getGridCoverageNames();
	}
	
	@Override
	public GeoGSFileType getGeoGSFileType(){
		return GeoGSFileType.RASTER;
	}
	
	@Override
	public List<GSPixel> getGeoData() throws IOException, TransformException{
		if(pixels.isEmpty())
			pixels = extractFeatures(); 
		return pixels;
	}

	@Override
	public boolean isCoordinateCompliant(IGeoGSFileIO<Number, Double> file) {
		return file.getCoordRefSystem().equals(this.getCoordRefSystem());
	}
	
	@Override
	public CoordinateReferenceSystem getCoordRefSystem() {
		return store.getCoordinateReferenceSystem();
	}
	
	public String[] getBandId(){
		return bandId;
	}

	// WARNING: not functional yet
//	public void cropFile(GeneralEnvelope envelop) throws IOException, TransformException{
//		CoverageProcessor processor = CoverageProcessor.getInstance();
//		ParameterValueGroup param = processor.getOperation("CoverageCrop").getParameters();
//		GeneralEnvelope crop = envelop;
//		
//		param.parameter("Source").setValue( coverage );
//		param.parameter("Envelope").setValue( crop );
//
//		this.coverage = (GridCoverage2D) processor.doOperation(param);	
//		this.featureList = extractFeatures(store, coverage);
//	}

	@Override
	public String toString(){
		String s = "";
		int numRows = store.getOriginalGridRange().getHigh(1) + 1;
		int numCols = store.getOriginalGridRange().getHigh(0) + 1;
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

	// ------------------------- inner utilities ------------------------- //

	/*
	 * Code for this has been past from stackexchange:
	 * http://gis.stackexchange.com/questions/106882/how-to-read-each-pixel-of-each-band-of-a-multiband-geotiff-with-geotools-java
	 * 
	 * TODO: setup an iterator to process data as bufferReader does
	 */
	private List<GSPixel> extractFeatures() {
		List<GSPixel> featureList = new ArrayList<>();
		
		int w = store.getOriginalGridRange().getHigh().getCoordinateValue(0)+1;
		int h = store.getOriginalGridRange().getHigh().getCoordinateValue(1)+1;

		int idx = 1;
		
		System.out.println("["+this.getClass().getSimpleName()+"] Theoretical space size to proceed raster file: width = "+w+" | heidth = "+h+" ("+(w*h)+")");
		int outsideCoverageCount = 0;
		
		for (int i=0; i<w; i++){
			for(int j=0; j<h; j++){
				double[] vals = new double[store.getGridCoverageCount()];
				try {
					coverage.evaluate(new GridCoordinates2D(i, j), vals);
				} catch (PointOutsideCoverageException e) {
					//e.printStackTrace();
					outsideCoverageCount++;
					continue;
				}
				Double[] valsN = new Double[vals.length];
				for(int k = 0; k < vals.length; k++)
					valsN[k] = vals[k];
				
				featureList.add(new GSPixel(i, j, valsN, store.getCoordinateReferenceSystem()));
				
				if(DoubleStream.of(vals).mapToObj(Double::valueOf).anyMatch(val -> val.isNaN() || val <= 0d || val == null))
					System.out.println("["+this.getClass().getSimpleName()+"] Strange value: "+Arrays.toString(vals).toString());
				if(idx % 1000000 == 0)
					System.out.println("["+this.getClass().getSimpleName()+"] "+idx+" px transposed (vals = "+Arrays.toString(vals).toString()+")");
			}
		}
		System.out.println("["+this.getClass().getSimpleName()+"]"+(w*h-outsideCoverageCount)+" proceed raster's pixels");
		
		return featureList;
	}

}
