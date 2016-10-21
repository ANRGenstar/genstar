package io.data.survey;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;

class CsvInputHandler implements IGSSurvey {
	
	private final char DEFAULT_SEPARATOR = ',';
	private List<String[]> dataTable;
	private CSVReader reader;
	private String surveyFileName;
	
	protected CsvInputHandler(String fileName) throws IOException{
		reader = new CSVReader(new FileReader(fileName), DEFAULT_SEPARATOR);
		dataTable = reader.readAll();
		surveyFileName = fileName;
	}
	
	protected CsvInputHandler(String fileName, char csvSeparator) throws IOException{
		reader = new CSVReader(new FileReader(fileName), csvSeparator);
		dataTable = reader.readAll();
		surveyFileName = fileName;
	}
	
	protected CsvInputHandler(File file) throws IOException {
		reader = new CSVReader(new FileReader(file), DEFAULT_SEPARATOR);
		dataTable = reader.readAll();
		surveyFileName = file.getName();
	}
	
	protected CsvInputHandler(File file, char csvSeparator) throws IOException {
		reader = new CSVReader(new FileReader(file), csvSeparator);
		dataTable = reader.readAll();
		surveyFileName = file.getName();
	}

	protected CsvInputHandler(String fileName, InputStream surveyIS) throws IOException {
		reader = new CSVReader(new InputStreamReader(surveyIS), DEFAULT_SEPARATOR);
		dataTable = reader.readAll();
		surveyFileName = fileName;
	}

// ------------------------ unique value parser ------------------------ //
	
	@Override
	public String read(int rowIndex, int columnIndex){
		return dataTable.get(rowIndex)[columnIndex].trim();
	}
	
// ------------------------ Line-parser methods ------------------------ //
	
	@Override
	public List<String> readLine(int rowNum) {
		List<String> line = new ArrayList<>(Arrays.asList(dataTable.get(rowNum)));
		return line;
	}

	@Override
	public List<List<String>> readLines(
			int fromFirstRowIndex, int toLastRowIndex) {
		List<List<String>> lines = new ArrayList<List<String>>();
		for (int i = fromFirstRowIndex; i < toLastRowIndex; i++)
			lines.add(readLine(i));
		return lines;
	}
	
	@Override
	public List<String> readLines(
			int fromFirstRowIndex, int toLastRowIndex, 
			int columnIndex) {
		List<String> line = new ArrayList<>();
		for (int i = fromFirstRowIndex; i < toLastRowIndex; i++)
			line.add(this.read(i, columnIndex));
		return line;
	}

	@Override
	public List<List<String>> readLines(
			int fromFirstRowIndex, int toLastRowIndex, 
			int fromFirstColumnIndex, int toLastColumnIndex) {
		List<List<String>> lines = new ArrayList<List<String>>();
		for(int i = fromFirstRowIndex; i < toLastRowIndex; i++)
			lines.add(new ArrayList<String>(this.readLine(i).subList(fromFirstColumnIndex, toLastColumnIndex)));
		return lines;
	}

// ------------------------ Column-parser methods ------------------------ //
	
	@Override
	public List<String> readColumn(int columnIndex) {
		List<String> column = new ArrayList<String>();
		Iterator<String[]> it = dataTable.iterator();
		while(it.hasNext()){
			String[] line = it.next();
			column.add(line[columnIndex]);
		}
		return column;
	}

	@Override
	public List<List<String>> readColumns(
			int fromFirstColumnIndex, int toLastColumnIndex) {
		List<List<String>> columns = new ArrayList<List<String>>();
		for(int i = fromFirstColumnIndex; i < toLastColumnIndex; i++)
			columns.add(this.readColumn(i));
		return columns;
	}
	
	@Override
	public List<String> readColumns(
			int fromFirstColumnIndex, int toLastColumnIndex, 
			int rowIndex) {
		List<String> column = new ArrayList<String>();
		for(int i = fromFirstColumnIndex; i < toLastColumnIndex; i++)
			column.add(this.read(rowIndex, i));
		return column;
	}

	@Override
	public List<List<String>> readColumns(
			int fromFirstLine, int toLastLine, int fromFirstVariable,
			int toLastVariable) {
		List<List<String>> columns = new ArrayList<List<String>>();
		for(int i = fromFirstVariable; i < toLastVariable; i++)
			columns.add(new ArrayList<String>(this.readColumn(i).subList(fromFirstLine, toLastLine)));
		return columns;
	}
	
// -----------------------------

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return surveyFileName;
	}
	
	@Override
	public int getLastRowIndex(){
		return dataTable.size() - 1;
	}
	
	@Override
	public int getLastColumnIndex() {
		if(dataTable.isEmpty())
			return 0;
		String[] firstRow = dataTable.get(0);
		return firstRow.length - 1;
	}
	
	@Override
	public String toString(){
		String s = "";
		s+="Survey name: "+getName()+"\n";
		s+="\tline number: "+dataTable.size();
		s+="\tcolumn number: "+dataTable.get(0).length;
		return s;
	}

}
