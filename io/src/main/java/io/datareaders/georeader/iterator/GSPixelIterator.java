package io.datareaders.georeader.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.geotools.coverage.grid.GridCoordinates2D;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.opengis.coverage.PointOutsideCoverageException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import io.datareaders.georeader.geodat.GSPixel;

public class GSPixelIterator implements Iterator<GSPixel> {

	private GridCoverage2D coverage;
	
	private final int nbBands;
	private final int maxWidth;
	private final int maxHeight;
	private final CoordinateReferenceSystem crs;
	
	private int w, h = 0;

	public GSPixelIterator(AbstractGridCoverage2DReader store, GridCoverage2D coverage) {
		this.nbBands = store.getGridCoverageCount();
		this.maxWidth = store.getOriginalGridRange().getHigh().getCoordinateValue(0)+1;
		this.maxHeight = store.getOriginalGridRange().getHigh().getCoordinateValue(1)+1;
		this.crs = store.getCoordinateReferenceSystem();
		this.coverage = coverage;
	}

	public GSPixelIterator(AbstractGridCoverage2DReader store, GridCoverage2D coverage,
			CoordinateReferenceSystem crs) {
		this.nbBands = store.getGridCoverageCount();
		this.maxWidth = store.getOriginalGridRange().getHigh().getCoordinateValue(0)+1;
		this.maxHeight = store.getOriginalGridRange().getHigh().getCoordinateValue(1)+1;
		this.crs = crs;
		this.coverage = coverage;
	}

	@Override
	public boolean hasNext() {
		if(w < maxWidth || h < maxHeight)
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
		if(w < maxWidth) w++; else{ h++; w = 0; }
		try {
			if(coverage.getGridGeometry().getGridRange2D().contains(w, h))
				coverage.evaluate(new GridCoordinates2D(w, h), vals);
		} catch (PointOutsideCoverageException e) {
			e.printStackTrace();
		}
		Double[] valsN = new Double[vals.length];
		for(int k = 0; k < vals.length; k++)
			valsN[k] = vals[k];
		
		return new GSPixel(w, h, valsN, crs);
	}

}
