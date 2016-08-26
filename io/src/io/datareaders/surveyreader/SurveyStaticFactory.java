package io.datareaders.surveyreader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import io.datareaders.surveyreader.exception.InputFileNotSupportedException;

public class SurveyStaticFactory {
		
	public static IGSSurvey getSurvey(File file) throws IOException, InputFileNotSupportedException, InvalidFormatException{
		if(file.getName().contains("xlsx"))
			return new XlsxInputHandler(file);
		if(file.getName().contains("xls"))
			return new XlsInputHandler(file);
		if(file.getName().contains("csv"))
			return new CsvInputHandler(file);
		else throw new InputFileNotSupportedException();
	}

	public static IGSSurvey getSurvey(String fileName, InputStream surveyIS) throws InputFileNotSupportedException, IOException {
		if(fileName.contains("xlsx"))
			return new XlsxInputHandler(surveyIS);
		if(fileName.contains("xls"))
			return new XlsInputHandler(surveyIS);
		if(fileName.contains("csv"))
			return new CsvInputHandler(fileName, surveyIS);
		else throw new InputFileNotSupportedException();
	}
	
	public static IGSSurvey getSurvey(File file, char csvSeparator) throws InputFileNotSupportedException, IOException, InvalidFormatException{
		if(file.getName().contains("xlsx"))
			return new XlsxInputHandler(file);
		if(file.getName().contains("xls"))
			return new XlsInputHandler(file);
		if(file.getName().contains("csv"))
			return new CsvInputHandler(file, csvSeparator);
		else throw new InputFileNotSupportedException();
	}
	
}
