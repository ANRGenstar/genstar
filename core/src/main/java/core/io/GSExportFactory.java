package core.io;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.geotools.feature.SchemaException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import core.io.geo.GeofileFactory;
import core.io.geo.RasterFile;
import core.io.geo.ShapeFile;
import core.io.geo.entity.GSFeature;

public class GSExportFactory {
	/** djshklkfhdsl*/
	
	public static ShapeFile createShapeFile(File shapefile, Collection<GSFeature> features) 
			throws IOException, SchemaException {
		return new GeofileFactory().createShapeFile(shapefile, features);
	}
	
	public static RasterFile createGeotiffFile(File geotiffile, float[][] pixels, CoordinateReferenceSystem crs) 
			throws IllegalArgumentException, IOException, TransformException{
		return new GeofileFactory().createRasterfile(geotiffile, pixels, RasterFile.DEF_NODATA.floatValue(), crs);
	}

}
