package io.data.writers;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.geotools.feature.SchemaException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import io.data.geo.GeofileFactory;
import io.data.geo.GeotiffFile;
import io.data.geo.ShapeFile;
import io.data.geo.attribute.GSFeature;

public class GSExportFactory {
	
	public static ShapeFile createShapeFile(File shapefile, Collection<GSFeature> features) 
			throws IOException, SchemaException {
		return new GeofileFactory().getShapeFile(shapefile, features);
	}
	
	public static GeotiffFile createGeotiffFile(File geotiffile, float[][] pixels, CoordinateReferenceSystem crs) 
			throws IllegalArgumentException, IOException, TransformException{
		return new GeofileFactory().getGeofile(geotiffile, pixels, crs);
	}

}
