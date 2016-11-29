package core.io;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.geotools.feature.SchemaException;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import core.io.geo.GeofileFactory;
import core.io.geo.RasterFile;
import core.io.geo.ShapeFile;
import core.io.geo.entity.GSFeature;
import core.metamodel.IPopulation;

public class GSExportFactory {

	
	public static ShapeFile createShapeFile(File shapefile, Collection<GSFeature> features) 
			throws IOException, SchemaException {
		return new GeofileFactory().createShapeFile(shapefile, features);
	}
	
	public static RasterFile createGeotiffFile(File geotiffile, float[][] pixels, ReferencedEnvelope envelope, CoordinateReferenceSystem crs) 
			throws IllegalArgumentException, IOException, TransformException{
		return new GeofileFactory().createRasterfile(geotiffile, pixels, RasterFile.DEF_NODATA.floatValue(), envelope, crs);
	}
	
	public static ShapeFile createShapeFile(File shapefile, @SuppressWarnings("rawtypes") IPopulation population, CoordinateReferenceSystem crs) 
			throws IOException, SchemaException {
		return new GeofileFactory().createShapeFile(shapefile, population, crs);  
	}
	

}
