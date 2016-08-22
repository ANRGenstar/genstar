package io.datareaders.georeader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import org.geotools.coverage.grid.GridCoordinates2D;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridEnvelope2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.OverviewPolicy;
import org.geotools.factory.Hints;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.opengis.coverage.grid.GridCoordinates;
import org.opengis.coverage.grid.GridEnvelope;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class GeotiffFileIO implements ISPLFileIO<Feature> {

	private final AbstractGridCoverage2DReader store;
	private final GridCoverage2D coverage;
	private final List<Feature> featureList;
	private String[] bandId;

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
		this.featureList = extractFeatures(store, coverage);
		this.bandId = this.store.getGridCoverageNames();
	}
	
	@Override
	public List<Feature> getFeatures(){
		return featureList;
	}

	@Override
	public boolean isCoordinateCompliant(ISPLFileIO<Feature> file) {
		return file.getCoordRefSystem().equals(this.getCoordRefSystem());
	}
	
	@Override
	public CoordinateReferenceSystem getCoordRefSystem() {
		return store.getCoordinateReferenceSystem();
	}
	
	public String[] getBandId(){
		return bandId;
	}

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
	 * Code for this has been past from these stackexchange answers:
	 * 
	 * 1) http://gis.stackexchange.com/questions/106882/how-to-read-each-pixel-of-each-band-of-a-multiband-geotiff-with-geotools-java
	 * 2) http://gis.stackexchange.com/questions/114598/creating-point-in-shapefile-from-latitude-longitude-using-geotools
	 * 
	 */
	private List<Feature> extractFeatures(AbstractGridCoverage2DReader store, GridCoverage2D coverage) throws IOException, TransformException{
		List<Feature> featureList = new ArrayList<>();
		
		
		GridEnvelope dimensions = store.getOriginalGridRange();
		GridCoordinates maxDimensions = dimensions.getHigh();
		int w = maxDimensions.getCoordinateValue(0)+1;
		int h = maxDimensions.getCoordinateValue(1)+1;
		int numBands = store.getGridCoverageCount();

		GridGeometry2D geometry = coverage.getGridGeometry();

		SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();

		//set the name
		b.setName( "FeatureFromRaster" );
		//add a geometry property
		b.setCRS(store.getCoordinateReferenceSystem()); // set crs first
		b.add( "location", Point.class ); // then add geometry
		//add band informations
		b.add( "band", double[].class);
		//build the type
		final SimpleFeatureType TYPE = b.buildFeatureType();
		
		SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);
		GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
		int idx = 0;
		
		IntStream intWidthStream = IntStream.range(0, w);
		IntStream intHeighStream = IntStream.range(0, h);

		for (int i=0; i<w; i++) {
			for (int j=0; j<h; j++) {

				Envelope2D pixelEnvelop =
						geometry.gridToWorld(new GridEnvelope2D(i, j, 1, 1));

				double lat = pixelEnvelop.getCenterY();
				double lon = pixelEnvelop.getCenterX();

				double[] vals = new double[numBands];
				coverage.evaluate(new GridCoordinates2D(i, j), vals);

				Point point = geometryFactory.createPoint(new Coordinate(lat, lon));
				featureBuilder.add(point);
				featureBuilder.add(vals);
				
				featureList.add(featureBuilder.buildFeature("pixel"+(idx++)));
				System.out.println(idx);
			}
		}
		return featureList;
	}

}
