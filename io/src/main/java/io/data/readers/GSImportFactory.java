package io.data.readers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.opengis.referencing.operation.TransformException;

import io.data.geo.GeofileFactory;
import io.data.geo.IGSGeofile;
import io.data.geo.ShapeFile;
import io.data.readers.exception.InvalidFileTypeException;
import io.data.survey.IGSSurvey;
import io.data.survey.SurveyFactory;

public class GSImportFactory {

	private static SurveyFactory sf = new SurveyFactory();
	private static GeofileFactory gf = new GeofileFactory();

	
	////////////////////////////////////////////////////////////////	
	// ------------------ SURVEY STATIC IMPORT ------------------ //
	////////////////////////////////////////////////////////////////	
	
	
	public static IGSSurvey getSurvey(File surveyFile) throws InvalidFormatException, IOException, InvalidFileTypeException {
		return sf.getSurvey(surveyFile);
	}
	
	public static IGSSurvey getSurvey(File file, char csvSeparator) throws InvalidFormatException, IOException, InvalidFileTypeException {
		return sf.getSurvey(file, csvSeparator);
	}
	
	public static IGSSurvey getSurvey(Path surveyPathFile) throws InvalidFormatException, IOException, InvalidFileTypeException {
		File file = surveyPathFile.toFile();
		if(file.exists())
			return getSurvey(file);
		throw new FileNotFoundException("The path "+surveyPathFile.toString()+" does not represent a valid path");
	}
	
	public static IGSSurvey getSurvey(Path surveyPathFile, char csvSeparator) throws InvalidFormatException, IOException, InvalidFileTypeException {
		File file = surveyPathFile.toFile();
		if(file.exists())
			return getSurvey(file, csvSeparator);
		throw new FileNotFoundException("The path "+surveyPathFile.toString()+" does not represent a valid path");
	}
	
	public static IGSSurvey getSurvey(String surveyStringPathFile) throws InvalidFormatException, IOException, InvalidFileTypeException {
		return getSurvey(Paths.get(surveyStringPathFile));
	}
	
	public static IGSSurvey getSurvey(String surveyStringPathFile, char csvSeparator) throws InvalidFormatException, IOException, InvalidFileTypeException {
		return getSurvey(Paths.get(surveyStringPathFile), csvSeparator);
	}
	
	public static IGSSurvey getSurvey(String fileName, InputStream inputStream) throws IOException, InvalidFileTypeException {
		return sf.getSurvey(fileName, inputStream);
	}

	
	////////////////////////////////////////////////////////////////
	// ----------------- GEODATA STATIC IMPORT ------------------ //
	////////////////////////////////////////////////////////////////
	
	
	public static IGSGeofile getGeofile(File geofile) throws IllegalArgumentException, TransformException, IOException, InvalidFileTypeException{
		return gf.getGeofile(geofile);
	}
	
	public static IGSGeofile getGeofile(Path geofilePath) throws IllegalArgumentException, TransformException, IOException, InvalidFileTypeException {
		File file = geofilePath.toFile();
		if(file.exists())
			return getGeofile(file);
		throw new FileNotFoundException("The path "+geofilePath.toString()+" does not represent a valid path");
	}
	
	public static IGSGeofile getGeofile(String geofileStringPath) throws IllegalArgumentException, TransformException, IOException, InvalidFileTypeException {
		return getGeofile(Paths.get(geofileStringPath));
	}
	
	// ------------------- shapefile import ------------------- //
	
	public static ShapeFile getShapeFile(File shapefile) throws IOException, InvalidFileTypeException{
		return gf.getShapeFile(shapefile);
	}
	
	public static ShapeFile getShapeFile(Path shapefilePath) throws IOException, InvalidFileTypeException {
		File file = shapefilePath.toFile();
		if(file.exists())
			return getShapeFile(file);
		throw new FileNotFoundException("The path "+shapefilePath.toString()+" does not represent a valid path");
	}
	
	public static ShapeFile getShapeFile(String shapefileStringPath) throws IOException, InvalidFileTypeException {
		return getShapeFile(Paths.get(shapefileStringPath));
	}
	
}
