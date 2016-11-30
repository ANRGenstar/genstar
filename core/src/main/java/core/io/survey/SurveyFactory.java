/*********************************************************************************************
 *
 * 'SurveyFactory.java, in plugin core, is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package core.io.survey;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import core.io.exception.InvalidFileTypeException;

/**
 * 
 * TODO: make instantiation of survey possible, e.g. to export sample or even the whole population as a survey, may be
 * think of a template for statistical report (see the various toString method of distribution and population in Gospl)
 * 
 * @author kevinchapuis
 *
 */
public class SurveyFactory {

	private static String CSV_EXT = ".csv";
	private static String XLS_EXT = ".xls";
	private static String XLSX_EXT = ".xlsx";

	private final List<String> supportedFileFormat;

	public SurveyFactory() {
		supportedFileFormat = Arrays.asList(CSV_EXT, XLS_EXT, XLSX_EXT);
	}

	public IGSSurvey getSurvey(final File file) throws IOException, InvalidFileTypeException, InvalidFormatException {
		if (file.getName().endsWith(XLSX_EXT))
			return new XlsxInputHandler(file);
		if (file.getName().endsWith(XLS_EXT))
			return new XlsInputHandler(file);
		if (file.getName().endsWith(CSV_EXT))
			return new CsvInputHandler(file);
		final String[] pathArray = file.getPath().split(File.separator);
		throw new InvalidFileTypeException(pathArray[pathArray.length - 1], supportedFileFormat);
	}

	public IGSSurvey getSurvey(final File file, final char csvSeparator) throws IOException, InvalidFileTypeException {
		if (file.getName().endsWith(XLSX_EXT))
			return new XlsxInputHandler(file);
		if (file.getName().endsWith(XLS_EXT))
			return new XlsInputHandler(file);
		if (file.getName().endsWith(CSV_EXT))
			return new CsvInputHandler(file, csvSeparator);
		final String[] pathArray = file.getPath().split(File.separator);
		throw new InvalidFileTypeException(pathArray[pathArray.length - 1], supportedFileFormat);
	}

	public IGSSurvey getSurvey(final String fileName, final InputStream surveyIS)
			throws IOException, InvalidFileTypeException {
		if (fileName.endsWith(XLSX_EXT))
			return new XlsxInputHandler(surveyIS);
		if (fileName.endsWith(XLS_EXT))
			return new XlsInputHandler(surveyIS);
		if (fileName.endsWith(CSV_EXT))
			return new CsvInputHandler(fileName, surveyIS);
		throw new InvalidFileTypeException(fileName, supportedFileFormat);
	}

	public List<String> getSupportedFileFormat() {
		return supportedFileFormat;
	}

}
