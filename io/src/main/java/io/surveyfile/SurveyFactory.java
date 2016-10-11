package io.surveyfile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import io.datareaders.exception.InvalidFileTypeException;

/**
 * 
 * TODO: make instantiation of survey possible, e.g. to export sample or even the whole population as a survey, 
 * may be think of a template for statistical report (see the various toString method of distribution 
 * and population in Gospl)
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
		
	public IGSSurvey getSurvey(File file) throws IOException, InvalidFileTypeException, InvalidFormatException {
		if(file.getName().contains(XLSX_EXT))
			return new XlsxInputHandler(file);
		if(file.getName().contains(XLS_EXT))
			return new XlsInputHandler(file);
		if(file.getName().contains(CSV_EXT))
			return new CsvInputHandler(file);
		String[] pathArray = file.getPath().split(File.separator);
		throw new InvalidFileTypeException(pathArray[pathArray.length-1], supportedFileFormat);
	}

	public IGSSurvey getSurvey(File file, char csvSeparator) throws IOException, InvalidFileTypeException, InvalidFormatException {
		if(file.getName().contains(XLSX_EXT))
			return new XlsxInputHandler(file);
		if(file.getName().contains(XLS_EXT))
			return new XlsInputHandler(file);
		if(file.getName().contains("csv"))
			return new CsvInputHandler(file, csvSeparator);
		String[] pathArray = file.getPath().split(File.separator);
		throw new InvalidFileTypeException(pathArray[pathArray.length-1], supportedFileFormat);
	}
	
	public IGSSurvey getSurvey(String fileName, InputStream surveyIS) throws IOException, InvalidFileTypeException {
		if(fileName.contains(XLSX_EXT))
			return new XlsxInputHandler(surveyIS);
		if(fileName.contains(XLS_EXT))
			return new XlsInputHandler(surveyIS);
		if(fileName.contains(CSV_EXT))
			return new CsvInputHandler(fileName, surveyIS);
		throw new InvalidFileTypeException(fileName, supportedFileFormat);
	}
	
	public List<String> getSupportedFileFormat(){
		return supportedFileFormat;
	}
	
}
