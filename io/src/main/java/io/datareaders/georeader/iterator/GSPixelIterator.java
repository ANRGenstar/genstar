package io.datareaders.georeader.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.geotools.coverage.grid.GridCoordinates2D;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridEnvelope2D;
import org.geotools.geometry.Envelope2D;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import io.datareaders.georeader.geodat.GSPixel;

public class GSPixelIterator implements Iterator<GSPixel> {

	private GridCoverage2D coverage;

	private final int nbBands;
	private final int maxWidth, originWidth;
	private final int maxHeight, originHeight;
	private final CoordinateReferenceSystem crs;

	private int w, h;

	public GSPixelIterator(int nbBands, GridCoverage2D coverage,
			CoordinateReferenceSystem crs) {
		this.nbBands = nbBands;
		this.originWidth = coverage.getGridGeometry().getGridRange2D().x;
		this.maxWidth = w + coverage.getGridGeometry().getGridRange2D().width;
		this.w = this.originWidth;
		this.originHeight = coverage.getGridGeometry().getGridRange2D().y;
		this.maxHeight = h + coverage.getGridGeometry().getGridRange2D().height;
		this.h = this.originHeight;
		this.crs = crs;
		this.coverage = coverage;
	}
	
	public GSPixelIterator(int nbBands, GridCoverage2D coverage) {
		this(nbBands, coverage, coverage.getCoordinateReferenceSystem());
	}

	@Override
	public boolean hasNext() {
		// If there is no x pixel left at least one y must remains & if no y pixel left at least one x must remains
		if(w < maxWidth && h < maxHeight - 1 || h < maxHeight && w < maxWidth - 1)
			return true;
		return false;
	}

	/*
	 * Code for this has been past from stackexchange:
	 * http://gis.stackexchange.com/questions/106882/how-to-read-each-pixel-of-each-band-of-a-multiband-geotiff-with-geotools-java
	 */
	@Override
	public GSPixel next() {
		if(!hasNext())
			throw new NoSuchElementException();
		double[] vals = new double[nbBands];
		if(w == maxWidth - 1) {h++; w=originWidth;} else {w++;} 
		coverage.evaluate(new GridCoordinates2D(w, h), vals);
		Double[] valsN = new Double[vals.length];
		for(int k = 0; k < vals.length; k++)
			valsN[k] = vals[k];

		Envelope2D pixelEnvelop = null;
		try {
			pixelEnvelop = coverage.getGridGeometry().gridToWorld(new GridEnvelope2D(w, h, 1, 1));
		} catch (TransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return new GSPixel(pixelEnvelop.getCenterX(), pixelEnvelop.getCenterY(), valsN, crs);
	}

}
