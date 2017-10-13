
package core.metamodel.pop.io;

import java.util.List;
import java.util.Map;
import java.util.Set;

import core.metamodel.pop.DemographicAttribute;
import core.metamodel.value.IValue;

/**
 * Main interface for access to survey as a table (List of list) 
 * Data are access through line (raw) and variable (column) indexes.
 * 
 * @author kevinchapuis
 *
 */
public interface IGSSurvey {
	
	public String getName();
	
	public String getSurveyFilePath();
	
	public GSSurveyType getDataFileType();
	public int getLastRowIndex();
	public int getLastColumnIndex();
	public int getFirstRowIndex();
	public int getFirstColumnIndex();

	/**
	 * Returns, for the underlying file format, the indices of each column associated with its possible values
	 * @param attributes
	 * @return
	 */
	public Map<Integer, Set<IValue>> getColumnHeaders(Set<DemographicAttribute<? extends IValue>> attributes);
	

	/**
	 * Returns, for the underlying file format, the indices of each row associated with its possible values
	 * @param attributes
	 * @return
	 */
	public Map<Integer, Set<IValue>> getRowHeaders(Set<DemographicAttribute<? extends IValue>> attributes);
	
	/**
	 * Retrieves column headers from a sample data file
	 */
	public Map<Integer, DemographicAttribute<? extends IValue>> getColumnSample(Set<DemographicAttribute<? extends IValue>> attributes);
	
	/**
	 * return the unique value associated to line at {@code rowIndex} and column at {@code columnIndex}
	 * 
	 * @param rowIndex
	 * @param columnIndex
	 * @return {@link String}
	 */
	public String read(int rowIndex, int columnIndex);
	
	/**
	 * Return an ordered list of String that gives the content 
	 * of the line at the given index (i.e. at row {@link rowNum}) of the survey.  
	 * Other format of cell content is read as a null String
	 * 
	 * 0-based count of rows
	 * 
	 * @param rowIndex
	 * @return List<String>
	 */
	public List<String> readLine(int rowIndex);

	/**
	 * Return an ordered list of lines (a list of string) from {@code fromFirstRowIndex} inclusive to 
	 * {@code toLastRowIndex} exclusive 
	 * 0-based count of rows
	 * 
	 * @param fromFirstRowIndex
	 * @param toLastRowIndex
	 * @return List<List<String>>
	 */
	public List<List<String>> readLines(
			int fromFirstRowIndex, int toLastRowIndex);

	/**
	 * Return an ordered list of lines (a {@link List} of {@link List} of {@link String}) from {@code fromFirstRowIndex} inclusive to 
	 * {@code toLastRowIndex} exclusive for the unique column at index {@code columnIndex}
	 * 
	 * @param fromFirstRowIndex
	 * @param toLastRowIndex
	 * @param columnIndex
	 * @return {@link List<String>}
	 */
	public List<String> readLines(
			int fromFirstRowIndex, int toLastRowIndex,
			int columnIndex);
	
	/**
	 * Return an ordered list of lines (a {@link List} of {@link List} of {@link String}) from {@code fromFirstRowIndex} inclusive to 
	 * {@code toLastRowIndex} exclusive, considering only column from {@code fromFirstColumnIndex} inclusive to 
	 * {@code toLastColumnIndex} exclusive
	 * 
	 * @param fromFirstRowIndex
	 * @param toLastRowIndex
	 * @param fromFirstColumnIndex
	 * @param toLastColumnIndex
	 * @return {@link List<List<String>}
	 */
	public List<List<String>> readLines(
			int fromFirstRowIndex, int toLastRowIndex, 
			int fromFirstColumnIndex, int toLastColumnIndex);

	/**
	 * Return an ordered list of variable value (a {@link List} of {@link String}). It represents data 
	 * associated to a column of the data table
	 * 
	 * @param columnIndex
	 * @return {@link List<String>}
	 */
	public List<String> readColumn(int columnIndex);

	/**
	 * Return an ordered list of variable (a list of list of variable values). It represents data 
	 * associated to n variable from {@code fromFirstColumnIndex} inclusive to {@code toLastColumnIndex} exclusive
	 * 
	 * @param fromFirstColumnIndex
	 * @param toLastColumnIndex
	 * @return {@link List<List<String>>}
	 */
	public List<List<String>> readColumns(
			int fromFirstColumnIndex, int toLastColumnIndex);

	/**
	 * Return an ordered list of variable value (a {@link List} of {@link String}) for a specific line. It represents 
	 * data associated to a n variable from {@code fromFirstColumnIndex} inclusive to {@code toLastColumnIndex} exclusive 
	 * for the unique line at index {@code rowIndex}
	 * 
	 * @param fromFirstColumnIndex
	 * @param toLastColumnIndex
	 * @param rowIndex
	 * @return {@link List<String>}
	 */
	public List<String> readColumns(
			int fromFirstColumnIndex, int toLastColumnIndex, 
			int rowIndex);
	
	/**
	 * Return an ordered list of variable for certain lines ({@see #readVariablesOnListTable(int, int)}) 
	 * It represents data associated to n variable from {@code fromFirstColumnIndex} inclusive to {@code toLastColumnIndex} 
	 * exclusive for m lines from {@code fromFirstRowIndex} inclusive to {@code toLastRowIndex} exclusive 
	 * 
	 * @param fromFirstRowIndex
	 * @param toLastRowIndex
	 * @param fromFirstColumnIndex
	 * @param toLastColumnIndex
	 * @return {@link List<List<String>>}
	 */
	public List<List<String>> readColumns(
			int fromFirstRowIndex, int toLastRowIndex, 
			int fromFirstColumnIndex, int toLastColumnIndex);
	
	
	@Override
	public String toString();
	
}