package io.geofile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.opengis.referencing.operation.TransformException;

import io.datareaders.exception.InvalidFileTypeException;

public class GeofileFactory {

	private static String SHAPEFILE_EXT = ".shp";
	private static String GEOTIFF_EXT = ".tiff";
	
	private final List<String> supportedFileFormat;
	
	public GeofileFactory(){
		supportedFileFormat = Arrays.asList(SHAPEFILE_EXT, GEOTIFF_EXT);
	}
	
	public IGSGeofile getGeofile(File file) throws IllegalArgumentException, TransformException, IOException, InvalidFileTypeException{
		if(file.getName().contains(".shp"))
			return new ShapeFile(file);
		if(file.getName().contains(".tif"))
			return new GeotiffFile(file);
		String[] pathArray = file.getPath().split(File.separator);
		throw new InvalidFileTypeException(pathArray[pathArray.length-1], supportedFileFormat);
	}
	
	/**
	 * TODO: fill in the method
	 * @return
	 * @throws IOException, InvalidFileTypeException 
	 */
	public ShapeFile getShapeFile(File shapefile) throws IOException, InvalidFileTypeException {
		if(shapefile.getName().contains(".shp"))
			return new ShapeFile(shapefile);
		String[] pathArray = shapefile.getPath().split(File.separator);
		throw new InvalidFileTypeException(pathArray[pathArray.length-1], Arrays.asList(SHAPEFILE_EXT));
	}
	
	public List<String> getSupportedFileFormat(){
		return supportedFileFormat;
	}
	
}
