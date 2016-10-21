package io.data.survey;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * Abstract class that define the general contract for
 * input manager to read workbook, whatever file
 * extension that pertains to the excel family product is. 
 * See concrete sub-class for extension file details handle 
 * through this Input manager program 
 * 
 * @author chapuisk
 *
 */
public abstract class AbstractXlsXlsxInputHandler implements IGSSurvey {

	protected Workbook wb;
	private String surveyFileName;
	private Sheet currentSheet;
	private DataFormatter dataFormatter = new DataFormatter();

// ------------------------ unique value parser ------------------------ //
	
		@Override
		public String read(int rowIndex, int columnIndex){
			Cell theCell = getCurrentSheet().getRow(rowIndex).getCell(columnIndex);
			if(theCell.getCellType() == Cell.CELL_TYPE_STRING)
				return theCell.getStringCellValue();
			else
				return getDataFormatter().formatCellValue(theCell);
		}
	
// ------------------------ Line-parser methods ------------------------ //
	
	@Override
	public List<String> readLine(int rowIndex) {
		List<String> line = new ArrayList<String>();
		Iterator<Cell> it = getCurrentSheet().getRow(rowIndex).cellIterator();
		int formerCellIndex = 0;
		while (it.hasNext()){
			Cell currentCell = it.next();
			
			int currentCellIndex = currentCell.getColumnIndex();
			if (currentCellIndex != 0 && currentCellIndex - formerCellIndex != 1){
				for(int i = formerCellIndex + 1; i < currentCellIndex; i++)
					line.add("");
			}
			
			if(currentCell.getCellType() == Cell.CELL_TYPE_STRING)
				line.add(currentCell.getStringCellValue());
			else
				line.add(getDataFormatter().formatCellValue(currentCell));
			
			formerCellIndex = currentCellIndex;
		}
		return line;
	}
	
	@Override
	public List<List<String>> readLines(
			int fromFirstRowIndex, int toLastRowIndex){
		List<List<String>> lines = new ArrayList<List<String>>();
		for(int i = fromFirstRowIndex; i < toLastRowIndex; i++)
			lines.add(this.readLine(i));
		return lines;
	}
	
	@Override
	public List<String> readLines(
			int fromFirstRowIndex, int toLastRowIndex,
			int columnIndex){
		List<String> lines = new ArrayList<>();
		for(int i = fromFirstRowIndex; i < toLastRowIndex; i++)
			lines.add(this.read(i, columnIndex));
		return lines;
	}
	
	@Override
	public List<List<String>> readLines(
			int fromFirstRowIndex, int toLastRowIndex, 
			int fromFirstColumnIndex, int toLastColumnIndex){
		List<List<String>> lines = new ArrayList<List<String>>();
		for(int i = fromFirstRowIndex; i < toLastRowIndex; i++){
			lines.add(new ArrayList<String>(this.readLine(i).subList(fromFirstColumnIndex, toLastColumnIndex)));
		}
		return lines;
	}

// ------------------------ Column-parser methods ------------------------ //
	
	@Override
	public List<String> readColumn(int columnIndex){
		List<String> column = new ArrayList<String>();
		Iterator<Row> it = getCurrentSheet().iterator();
		while (it.hasNext())
			column.add(readLine(it.next().getRowNum()).get(columnIndex));
		return column;
	}

	@Override
	public List<List<String>> readColumns(
			int fromFirstColumnIndex, int toLastColumnIndex){
		List<List<String>> columns = new ArrayList<List<String>>();
		for(int i = fromFirstColumnIndex; i < toLastColumnIndex; i++)
			columns.add(this.readColumn(i));
		return columns;
	}
	
	@Override
	public List<String> readColumns(
			int fromFirstColumnIndex, int toLastColumnIndex,
			int rowIndex){
		List<String> columns = new ArrayList<>();
		for(int i = fromFirstColumnIndex; i < toLastColumnIndex; i++)
			columns.add(read(rowIndex, i));
		return columns;
	}
	
	@Override
	public List<List<String>> readColumns(
			int fromFirstRowIndex, int toLastRowIndex, 
			int fromFirstColumnIndex, int toLastColumnIndex){
		List<List<String>> columns = new ArrayList<List<String>>();
		for(int i = fromFirstColumnIndex; i < toLastColumnIndex; i++)
			columns.add(new ArrayList<String>(this.readColumn(i).subList(fromFirstRowIndex, toLastRowIndex)));
		return columns;
	}

// ---------------------------- getter & setter ---------------------------- //
	
	@Override
	public String getName(){
		return surveyFileName;
	}
	
	public Workbook getWb(){
		return wb;
	}
	
	protected DataFormatter getDataFormatter(){
		return dataFormatter;
	}
	
	protected void setCurrentSheet(Sheet currentSheet){
		this.currentSheet = currentSheet;
	}
	
	protected Sheet getCurrentSheet(){
		return currentSheet;
	}
	
	@Override
	public int getLastRowIndex(){
		return currentSheet.getLastRowNum() - 1;
		/*int nb = 0;
		Iterator<Row> rowIt = currentSheet.rowIterator();
		Row lastRow = null;
		while(rowIt.hasNext()){
			lastRow = rowIt.next();
			nb++;
		}
		if(lastRow.getRowNum() + 1 == nb)
			return lastRow.getRowNum();
		else
			return nb - 1;*/
	}
	
	@Override
	public int getLastColumnIndex(){
		return currentSheet.rowIterator().next().getLastCellNum() - 1;
	}
	
	@Override
	public String toString(){
		String s = "";
		s+="Survey name: "+getName()+"\n";
		s+="\tline number: "+(getLastRowIndex()+1);
		s+="\tcolumn number: "+(getLastColumnIndex()+1);
		return s;
	}
}
