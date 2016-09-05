package io.datareaders.surveyreader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import io.datareaders.surveyreader.exception.InvalidFileTypeException;

public class SurveyStaticFactory {
	
	private static List<String> SUPPORTED_FILE_FORMAT = Arrays.asList(".csv", ".xls", ".xlsx");
		
	public static IGSSurvey getSurvey(File file) throws IOException, InvalidFileTypeException, InvalidFormatException {
		if(file.getName().contains("xlsx"))
			return new XlsxInputHandler(file);
		if(file.getName().contains("xls"))
			return new XlsInputHandler(file);
		if(file.getName().contains("csv"))
			return new CsvInputHandler(file);
		String[] pathArray = file.getPath().split(File.separator);
		throw new InvalidFileTypeException(pathArray[pathArray.length-1], SUPPORTED_FILE_FORMAT);
	}

	public static IGSSurvey getSurvey(String fileName, InputStream surveyIS) throws IOException, InvalidFileTypeException {
		if(fileName.contains("xlsx"))
			return new XlsxInputHandler(surveyIS);
		if(fileName.contains("xls"))
			return new XlsInputHandler(surveyIS);
		if(fileName.contains("csv"))
			return new CsvInputHandler(fileName, surveyIS);
		throw new InvalidFileTypeException(fileName, SUPPORTED_FILE_FORMAT);
	}
	
	public static IGSSurvey getSurvey(File file, char csvSeparator) throws IOException, InvalidFileTypeException, InvalidFormatException {
		if(file.getName().contains("xlsx"))
			return new XlsxInputHandler(file);
		if(file.getName().contains("xls"))
			return new XlsInputHandler(file);
		if(file.getName().contains("csv"))
			return new CsvInputHandler(file, csvSeparator);
		String[] pathArray = file.getPath().split(File.separator);
		throw new InvalidFileTypeException(pathArray[pathArray.length-1], SUPPORTED_FILE_FORMAT);
	}
	
	public static List<String> getSupportedFileFormat(){
		return SUPPORTED_FILE_FORMAT;
	}
	
}
