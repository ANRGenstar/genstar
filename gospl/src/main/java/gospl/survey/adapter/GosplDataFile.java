package gospl.survey.adapter;

import java.io.File;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.nio.file.Paths;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import gospl.survey.GosplMetaDataType;
import io.datareaders.surveyreader.IGSSurvey;
import io.datareaders.surveyreader.SurveyStaticFactory;

public class GosplDataFile {

	private final String surveyFilePath;
	private char csvSeparator = ',';

	private final GosplMetaDataType dataFileType;

	private final int firstRowDataIndex;
	private final int firstColumnDataIndex;

	public GosplDataFile(String surveyFilePath, GosplMetaDataType dataFileType,  int firstRowDataIndex, int firstColumnDataIndex){
		this.surveyFilePath = surveyFilePath;
		this.dataFileType = dataFileType;
		this.firstRowDataIndex = firstRowDataIndex;
		this.firstColumnDataIndex = firstColumnDataIndex;
	}

	public GosplDataFile(File survey, GosplMetaDataType dataFileType, int firstRowDataIndex, int firstColumnDataIndex){
		this(survey.getAbsolutePath(), dataFileType, firstRowDataIndex, firstColumnDataIndex);
	}

	public GosplDataFile(String survey, GosplMetaDataType dataFileType,  int firstRowDataIndex, int firstColumnDataIndex, char csvSeparator){
		this(survey, dataFileType, firstRowDataIndex, firstColumnDataIndex);
		this.csvSeparator = csvSeparator;
	}

	public GosplDataFile(File survey, GosplMetaDataType dataFileType, int firstRowDataIndex, int firstColumnDataIndex, char csvSeparator){
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

	public GosplMetaDataType getDataFileType(){
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
	 * @throws InputFileNotSupportedException
	 */
	public IGSSurvey getSurvey() throws InvalidFormatException, IOException {
		return SurveyStaticFactory.getSurvey(new File(surveyFilePath), csvSeparator);
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
		GosplMetaDataType dataFileType = getDataFileType();
		char csvSeparator = getCsvSeparator();
		return new GosplDataFile(survey, dataFileType, getFirstRowDataIndex(), getFirstColumnDataIndex(), csvSeparator);
	}

}
