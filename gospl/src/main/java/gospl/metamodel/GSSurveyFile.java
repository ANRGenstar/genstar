package gospl.metamodel;

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
 * TODO: javadoc
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

	/**
	 * Construct a survey file to store and parse survey data and info. 
	 * <p>
	 * Stored properties are:
	 * <ul>
	 * <li> data file type as a {@link GSSurveyType} 
	 * <li> zero based row ({@code firstRowDataIndex}) and column ({@code firstColumnDataIndex}) headers index 
	 * <li> data file name
	 * </ul>
	 * <p> 
	 * Default csv separator is ',' to setup a different one choose {@link #GSSurveyFile(String, GSSurveyType, int, int, char)} 
	 * 
	 * @param surveyFilePath
	 * @param dataFileType
	 * @param firstRowDataIndex which is zero based
	 * @param firstColumnDataIndex which is zero based
	 */
	public GSSurveyFile(String surveyFilePath, GSSurveyType dataFileType,  int firstRowDataIndex, int firstColumnDataIndex){
		this.surveyFilePath = surveyFilePath;
		this.dataFileType = dataFileType;
		this.firstRowDataIndex = firstRowDataIndex;
		this.firstColumnDataIndex = firstColumnDataIndex;
	}

	/**
	 * work as {@link #GSSurveyFile(File, GSSurveyType, int, int)} but with {@link File} type argument
	 * 
	 * @see #GSSurveyFile(File, GSSurveyType, int, int)
	 * 
	 * @param survey
	 * @param dataFileType
	 * @param firstRowDataIndex
	 * @param firstColumnDataIndex
	 */
	public GSSurveyFile(File survey, GSSurveyType dataFileType, int firstRowDataIndex, int firstColumnDataIndex){
		this(survey.getAbsolutePath(), dataFileType, firstRowDataIndex, firstColumnDataIndex);
	}

	/**
	 * work as {@link #GSSurveyFile(File, GSSurveyType, int, int)} but with a specified {@code csvSeparator}
	 * 
	 * @see #GSSurveyFile(File, GSSurveyType, int, int)
	 * 
	 * @param survey
	 * @param dataFileType
	 * @param firstRowDataIndex
	 * @param firstColumnDataIndex
	 * @param csvSeparator
	 */
	public GSSurveyFile(String survey, GSSurveyType dataFileType,  int firstRowDataIndex, int firstColumnDataIndex, char csvSeparator){
		this(survey, dataFileType, firstRowDataIndex, firstColumnDataIndex);
		this.csvSeparator = csvSeparator;
	}

	/**
	 * work as {@link #GSSurveyFile(File, GSSurveyType, int, int)} but with {@link File} type argument
	 * and a specified {@code csvSeparator}
	 * 
	 * @see #GSSurveyFile(File, GSSurveyType, int, int)
	 * 
	 * @param survey
	 * @param dataFileType
	 * @param firstRowDataIndex
	 * @param firstColumnDataIndex
	 * @param csvSeparator
	 */
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
