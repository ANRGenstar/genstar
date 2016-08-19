package io.datareaders.surveyreader;

import java.util.List;

/**
 * Main interface for access to survey as a table (List of list) 
 * Data are access through line (raw) and variable (column) indexes.
 * 
 * @author kevinchapuis
 *
 */
public interface ISurvey {

	/**
	 * return the unique value associated to line at {@code rowIndex} and column at {@code columnIndex}
	 * 
	 * @param rowIndex
	 * @param columnIndex
	 * @return {@link String}
	 */
	public abstract String read(int rowIndex, int columnIndex);
	
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
	public abstract List<String> readLine(int rowIndex);

	/**
	 * Return an ordered list of lines (a list of string) from {@code fromFirstRowIndex} inclusive to 
	 * {@code toLastRowIndex} exclusive 
	 * 0-based count of rows
	 * 
	 * @param fromFirstRowIndex
	 * @param toLastRowIndex
	 * @return List<List<String>>
	 */
	public abstract List<List<String>> readLines(
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
	public abstract List<String> readLines(
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
	public abstract List<List<String>> readLines(
			int fromFirstRowIndex, int toLastRowIndex, 
			int fromFirstColumnIndex, int toLastColumnIndex);

	/**
	 * Return an ordered list of variable value (a {@link List} of {@link String}). It represents data 
	 * associated to a column of the data table
	 * 
	 * @param columnIndex
	 * @return {@link List<String>}
	 */
	public abstract List<String> readColumn(int columnIndex);

	/**
	 * Return an ordered list of variable (a list of list of variable values). It represents data 
	 * associated to n variable from {@code fromFirstColumnIndex} inclusive to {@code toLastColumnIndex} exclusive
	 * 
	 * @param fromFirstColumnIndex
	 * @param toLastColumnIndex
	 * @return {@link List<List<String>>}
	 */
	public abstract List<List<String>> readColumns(
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
	public abstract List<String> readColumns(
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
	public abstract List<List<String>> readColumns(
			int fromFirstRowIndex, int toLastRowIndex, 
			int fromFirstColumnIndex, int toLastColumnIndex);

	public abstract String getName();
	public abstract int getLastRowIndex();
	public abstract int getLastColumnIndex();
	
	@Override
	public abstract String toString();
	
}