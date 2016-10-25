package core.io.survey.configuration;

import java.io.File;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.nio.file.Paths;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import core.io.GSImportFactory;
import core.io.exception.InvalidFileTypeException;
import core.io.survey.IGSSurvey;

/**
 * 
 * TODO: move to io project and be more generic
 * 
 * @author kevinchapuis
 *
 */
public class GSSurveyFile {

	private final String surveyFilePath;
	private char csvSeparator = ',';

	private final GSSurveyType dataFileType;

	private final int firstRowDataIndex;
	private final int firstColumnDataIndex;

	public GSSurveyFile(String surveyFilePath, GSSurveyType dataFileType,  int firstRowDataIndex, int firstColumnDataIndex){
		this.surveyFilePath = surveyFilePath;
		this.dataFileType = dataFileType;
		this.firstRowDataIndex = firstRowDataIndex;
		this.firstColumnDataIndex = firstColumnDataIndex;
	}

	public GSSurveyFile(File survey, GSSurveyType dataFileType, int firstRowDataIndex, int firstColumnDataIndex){
		this(survey.getAbsolutePath(), dataFileType, firstRowDataIndex, firstColumnDataIndex);
	}

	public GSSurveyFile(String survey, GSSurveyType dataFileType,  int firstRowDataIndex, int firstColumnDataIndex, char csvSeparator){
		this(survey, dataFileType, firstRowDataIndex, firstColumnDataIndex);
		this.csvSeparator = csvSeparator;
	}

	public GSSurveyFile(File survey, GSSurveyType dataFileType, int firstRowDataIndex, int firstColumnDataIndex, char csvSeparator){
		this(survey.getAbsolutePath(), dataFileType, firstRowDataIndex, firstColumnDataIndex);
		this.csvSeparator = csvSeparator;
	}

// --------------------------- ACCESSOR --------------------------- //

	public String getSurveyFilePath(){
		return surveyFilePath;
	}
	
	public String getSurveyFileName() {
		return Paths.get(surveyFilePath).getFileName().toString();
	}

	public GSSurveyType getDataFileType(){
		return dataFileType;
	}

	public int getFirstRowDataIndex(){
		return firstRowDataIndex;
	}

	public int getFirstColumnDataIndex(){
		return firstColumnDataIndex;
	}

	public char getCsvSeparator(){
		return csvSeparator;
	}

	public void setCsvSeparator(char csvSeparator){
		this.csvSeparator = csvSeparator;
	}

// --------------------------- MAIN CONTRACT --------------------------- //

	/**
	 * Give the survey associated with this file
	 * 
	 * @return {@link IGSSurvey} - a concrete survey with row and column access methods
	 * @throws InvalidFormatException
	 * @throws IOException
	 * @throws InvalidFileTypeException 
	 * @throws InputFileNotSupportedException
	 */
	public IGSSurvey getSurvey() throws InvalidFormatException, IOException, InvalidFileTypeException {
		return GSImportFactory.getSurvey(new File(surveyFilePath), csvSeparator);
	}
	
// --------------------------- utility methods --------------------------- //
	
	@Override
	public String toString(){
		return getSurveyFileName()+" ("+getDataFileType()+")";
	}
	
// --------------------------- inner methods --------------------------- //

	/*
	 * Method that enable a safe serialization / deserialization of this java class <br/>
	 * The serialization process end up in xml file that represents a particular java <br/>
	 * object of this class; and the way back from xml file to java object. 
	 */
	protected Object readResolve() throws ObjectStreamException {
		String survey = getSurveyFilePath();
		GSSurveyType dataFileType = getDataFileType();
		char csvSeparator = getCsvSeparator();
		return new GSSurveyFile(survey, dataFileType, getFirstRowDataIndex(), getFirstColumnDataIndex(), csvSeparator);
	}

}
