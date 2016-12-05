package core.metamodel.pop.io;

import java.io.FileNotFoundException;
import java.io.ObjectStreamException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Wrapper class that encapsulate needed information for {@link IGSSurvey}
 * to be created from
 * <p>
 * Also define default value for variable that initialize survey readers,
 * like the index of sheet to read, headers index or csv separator character
 * 
 * @author kevinchapuis
 *
 */
public class GSSurveyWrapper {

	public static char DEFAULT_SEPARATOR = ',';
	public static int DEFAULT_SHEET_NB = 0;
	public static int FIRST_ROW_DATA = 1;
	public static int FIRST_COLUMN_DATA = 1;
	
	// Cannot set a default value to this two variables
	private final String absoluteFilePath;
	private final GSSurveyType surveyType;
	
	// Possible default value for these variables
	private final int sheetNb;
	private final char csvSeparator;
	private final int firstRowIndex;
	private final int firstColumnIndex;
	
	/**
	 * Inner constructor for deserialization
	 * 
	 * @param absoluteFilePath
	 * @param surveyType
	 */
	protected GSSurveyWrapper(String absoluteFilePath, GSSurveyType surveyType,
			int sheetNb, char csvSeparator, int firstRowIndex, int firstColumnIndex){
		this.absoluteFilePath = absoluteFilePath;
		this.surveyType = surveyType;
		this.sheetNb = sheetNb;
		this.csvSeparator = csvSeparator;
		this.firstRowIndex = firstRowIndex;
		this.firstColumnIndex = firstColumnIndex;
	}
	
	/**
	 * Wrapper for default survey file attribute
	 * 
	 * @param absoluteFilePath
	 */
	public GSSurveyWrapper(String absoluteFilePath, GSSurveyType surveyType) {
		this(absoluteFilePath, surveyType, DEFAULT_SHEET_NB,
				DEFAULT_SEPARATOR, FIRST_ROW_DATA, FIRST_COLUMN_DATA);
	}
	
	/**
	 * Wrapper for Excel type survey
	 * 
	 * @param absoluteFilePath
	 * @param sheetNb
	 * @param firstRowIndex
	 * @param firstColumnIndex
	 */
	public GSSurveyWrapper(String absoluteFilePath, GSSurveyType surveyType, 
			int sheetNb, int firstRowIndex, int firstColumnIndex) {
		this(absoluteFilePath, surveyType, sheetNb, DEFAULT_SEPARATOR, 
				firstRowIndex, firstColumnIndex);
	}
	
	/**
	 * Wrapper for csv type survey
	 * 
	 * @param absoluteFilePath
	 * @param csvSeparator
	 * @param firstRowIndex
	 * @param firstColumnIndex
	 */
	public GSSurveyWrapper(String absoluteFilePath, GSSurveyType surveyType, 
			char csvSeparator, int firstRowIndex, int firstColumnIndex) {
		this(absoluteFilePath, surveyType, DEFAULT_SHEET_NB, csvSeparator, 
				firstRowIndex, firstColumnIndex);
	}
	
	// ------------------- ACCESSOR ------------------- //
	
	/**
	 * Get the absolute {@link Path} to the file
	 * 
	 * @return
	 */
	public Path getAbsolutePath(){
		return Paths.get(absoluteFilePath);
	}
	
	/**
	 * Get the type of survey as a {@link GSSurveyType} enum
	 * 
	 * @see GSSurveyType
	 * @return
	 */
	public GSSurveyType getSurveyType(){
		return surveyType;
	}
	
	/**
	 * Get the absolute path to the file as a {@link String}
	 * 
	 * @return
	 */
	public String getAbsoluteStringPath(){
		return absoluteFilePath.toString();
	}
	
	/**
	 * Get the number associated with current sheet (from a tabular data
	 * frame Excel type)
	 * 
	 * @return
	 */
	public int getSheetNumber(){
		return sheetNb;
	}
	
	/**
	 * Give the csv separator character
	 * 
	 * @return
	 */
	public char getCsvSeparator() {
		return csvSeparator;
	}
	
	/**
	 * Get the first row data index. Former index denote then
	 * headers and the like
	 * 
	 * @return
	 */
	public int getFirstRowIndex(){
		return firstRowIndex;
	}
	
	/**
	 * Get the first column data index. Former index denote then
	 * info about the content of each row
	 * 
	 * @return
	 */
	public int getFirstColumnIndex(){
		return firstColumnIndex;
	}
	
	// ------------------------------------------------------- //
	
	/*
	 * Method that enable a safe serialization / deserialization of this java class <br/>
	 * The serialization process end up in xml file that represents a particular java <br/>
	 * object of this class; and the way back from xml file to java object. 
	 */
	protected Object readResolve() throws ObjectStreamException, FileNotFoundException, UnsupportedEncodingException {
		return new GSSurveyWrapper(getAbsoluteStringPath(), getSurveyType(), 
				getSheetNumber(), getCsvSeparator(), getFirstRowIndex(), 
				getFirstColumnIndex());
	}
	
}
