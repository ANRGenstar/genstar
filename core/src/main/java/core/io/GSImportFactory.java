/*********************************************************************************************
 *
 * 'GSImportFactory.java, in plugin core, is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package core.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.opengis.referencing.operation.TransformException;

import core.io.exception.InvalidFileTypeException;
import core.io.geo.GeofileFactory;
import core.io.geo.IGSGeofile;
import core.io.geo.ShapeFile;
import core.io.survey.IGSSurvey;
import core.io.survey.SurveyFactory;

public class GSImportFactory {

	private static SurveyFactory sf = new SurveyFactory();
	private static GeofileFactory gf = new GeofileFactory();

	////////////////////////////////////////////////////////////////
	// ------------------ SURVEY STATIC IMPORT ------------------ //
	////////////////////////////////////////////////////////////////

	public static IGSSurvey getSurvey(final File surveyFile)
			throws InvalidFormatException, IOException, InvalidFileTypeException {
		return sf.getSurvey(surveyFile);
	}

	public static IGSSurvey getSurvey(final File file, final char csvSeparator)
			throws IOException, InvalidFileTypeException {
		return sf.getSurvey(file, csvSeparator);
	}

	public static IGSSurvey getSurvey(final Path surveyPathFile)
			throws InvalidFormatException, IOException, InvalidFileTypeException {
		final File file = surveyPathFile.toFile();
		if (file.exists())
			return getSurvey(file);
		throw new FileNotFoundException("The path " + surveyPathFile.toString() + " does not represent a valid path");
	}

	public static IGSSurvey getSurvey(final Path surveyPathFile, final char csvSeparator)
			throws InvalidFormatException, IOException, InvalidFileTypeException {
		final File file = surveyPathFile.toFile();
		if (file.exists())
			return getSurvey(file, csvSeparator);
		throw new FileNotFoundException("The path " + surveyPathFile.toString() + " does not represent a valid path");
	}

	public static IGSSurvey getSurvey(final String surveyStringPathFile)
			throws InvalidFormatException, IOException, InvalidFileTypeException {
		return getSurvey(Paths.get(surveyStringPathFile));
	}

	public static IGSSurvey getSurvey(final String surveyStringPathFile, final char csvSeparator)
			throws InvalidFormatException, IOException, InvalidFileTypeException {
		return getSurvey(Paths.get(surveyStringPathFile), csvSeparator);
	}

	public static IGSSurvey getSurvey(final String fileName, final InputStream inputStream)
			throws IOException, InvalidFileTypeException {
		return sf.getSurvey(fileName, inputStream);
	}

	////////////////////////////////////////////////////////////////
	// ----------------- GEODATA STATIC IMPORT ------------------ //
	////////////////////////////////////////////////////////////////

	public static IGSGeofile getGeofile(final File geofile)
			throws IllegalArgumentException, TransformException, IOException, InvalidFileTypeException {
		return gf.getGeofile(geofile);
	}

	public static IGSGeofile getGeofile(final Path geofilePath)
			throws IllegalArgumentException, TransformException, IOException, InvalidFileTypeException {
		final File file = geofilePath.toFile();
		if (file.exists())
			return getGeofile(file);
		throw new FileNotFoundException("The path " + geofilePath.toString() + " does not represent a valid path");
	}

	public static IGSGeofile getGeofile(final String geofileStringPath)
			throws IllegalArgumentException, TransformException, IOException, InvalidFileTypeException {
		return getGeofile(Paths.get(geofileStringPath));
	}

	// ------------------- shapefile import ------------------- //

	public static ShapeFile getShapeFile(final File shapefile) throws IOException, InvalidFileTypeException {
		return gf.getShapeFile(shapefile);
	}

	public static ShapeFile getShapeFile(final Path shapefilePath) throws IOException, InvalidFileTypeException {
		final File file = shapefilePath.toFile();
		if (file.exists())
			return getShapeFile(file);
		throw new FileNotFoundException("The path " + shapefilePath.toString() + " does not represent a valid path");
	}

	public static ShapeFile getShapeFile(final String shapefileStringPath)
			throws IOException, InvalidFileTypeException {
		return getShapeFile(Paths.get(shapefileStringPath));
	}

}
