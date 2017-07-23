package gospl.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.crypto.RuntimeCryptoException;

import core.metamodel.pop.io.GSSurveyType;
import core.metamodel.pop.io.IGSSurvey;
import nl.knaw.dans.common.dbflib.CorruptedTableException;
import nl.knaw.dans.common.dbflib.Field;
import nl.knaw.dans.common.dbflib.IfNonExistent;
import nl.knaw.dans.common.dbflib.Record;
import nl.knaw.dans.common.dbflib.Table;
import nl.knaw.dans.common.dbflib.Type;

public class DBaseInputHandler implements IGSSurvey {

	private static Logger logger = LogManager.getLogger();

	private String databaseFilename;
	
	private Table table = null;
	private Map<Integer,String> idx2columnName = null;
	
	private GSSurveyType surveyType;
			
	public DBaseInputHandler(GSSurveyType surveyType, String databaseFilename) {

		this.databaseFilename = databaseFilename;
		
		this.surveyType = surveyType;
		
		this.table = null;
		this.idx2columnName = null;
	}
	
	public DBaseInputHandler(GSSurveyType surveyType, File databaseFile) {

		this.databaseFilename = databaseFile.getAbsolutePath();
		
		this.surveyType = surveyType;
		
		this.table = null;
		this.idx2columnName = null;
	}
	
	protected Table getDBFTable() {

		if (table == null) {
			table = new Table(new File(databaseFilename));
	
			try {
			    table.open(IfNonExistent.ERROR);
	
			    List<Field> fields = table.getFields();
			    
			    idx2columnName = new HashMap<>();
			    
			    for (int i=0; i<fields.size(); i++) {
			    	idx2columnName.put(i, fields.get(i).getName());
			    }
			    /*
			    for (final Field field : fields)
			    {
			        System.out.println("Name:         " + field.getName());
			        System.out.println("Type:         " + field.getType());
			        System.out.println("Length:       " + field.getLength());
			        System.out.println("DecimalCount: " + field.getDecimalCount());
			        System.out.println();
			    }
				*/
			    //table.getFields().
	
			    
			} catch (CorruptedTableException e) {
				e.printStackTrace();
				throw new IllegalArgumentException("the database "+databaseFilename+" seems corrupted", e);
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException("error reading data from database "+databaseFilename, e);
			} 
			
		}
		return table;
		
	}

	@Override
	public String getName() {
		return new File(databaseFilename).getName();
	}

	@Override
	public String getSurveyFilePath() {
		return databaseFilename;
	}

	@Override
	public void setSurveyFilePath(String string) {
		if (this.table != null)
			try {
				this.table.close();
			} catch (IOException e) {
				throw new RuntimeException("error while saving the dbf file "+this.databaseFilename, e);
			}
		this.table = null;
		this.idx2columnName = null;
		this.databaseFilename = string;
	}

	@Override
	public GSSurveyType getDataFileType() {
		return surveyType;
	}

	@Override
	public int getLastRowIndex() {

		return getDBFTable().getRecordCount();
	}

	@Override
	public int getLastColumnIndex() {
		return getDBFTable().getFields().size();
	}

	@Override
	public int getFirstRowIndex() {
		return 0;
	}

	@Override
	public int getFirstColumnIndex() {
		return 0;
	}

	@Override
	public String read(int rowIndex, int columnIndex) {
		
		try {
			return getDBFTable().getRecordAt(rowIndex).getTypedValue(idx2columnName.get(columnIndex)).toString();
		} catch (CorruptedTableException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	@Override
	public List<String> readLine(int rowIndex) {
		
		Record record;
		try {
			record = getDBFTable().getRecordAt(rowIndex);
		} catch (CorruptedTableException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		
		return getDBFTable().getFields().stream().map(f -> record.getTypedValue(f.getName()).toString()).collect(Collectors.toList());
		
	}

	@Override
	public List<List<String>> readLines(int fromFirstRowIndex, int toLastRowIndex) {
		List<List<String>> res = new ArrayList<>(toLastRowIndex-fromFirstRowIndex);
		for (int i=fromFirstRowIndex; i<toLastRowIndex; i++)
			res.add(readLine(i));
		return res;
	}

	@Override
	public List<String> readLines(int fromFirstRowIndex, int toLastRowIndex, int columnIndex) {
		
		List<String> res = new ArrayList<>(toLastRowIndex-fromFirstRowIndex);

		final Table table = getDBFTable();
		final String colName = idx2columnName.get(columnIndex);
		
		for (int i=fromFirstRowIndex; i<toLastRowIndex; i++)
			try {
				res.add(table.getRecordAt(i).getTypedValue(colName).toString());
			} catch (CorruptedTableException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			
		return res;
	}

	@Override
	public List<List<String>> readLines(int fromFirstRowIndex, int toLastRowIndex, int fromFirstColumnIndex,
			int toLastColumnIndex) {
		
		List<List<String>> res = new ArrayList<>(toLastRowIndex-fromFirstRowIndex);

		final Table table = getDBFTable();
		
		final List<String> colNames = table.getFields().subList(fromFirstColumnIndex, toLastColumnIndex).stream().map(f -> f.getName()).collect(Collectors.toList());
		
		for (int i=fromFirstRowIndex; i<toLastRowIndex; i++)
			try {
				Record record = table.getRecordAt(i);
				res.add(colNames.stream().map(n -> record.getTypedValue(n).toString()).collect(Collectors.toList()) );
			} catch (CorruptedTableException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			
		return res;
	}

	@Override
	public List<String> readColumn(int columnIndex) {
		
		final Table table = getDBFTable();
		List<String> res = new ArrayList<>(table.getRecordCount());
		
		final String colName = idx2columnName.get(columnIndex);
		
		for (int i=0; i<table.getRecordCount(); i++)
			try {
				res.add(table.getRecordAt(i).getTypedValue(colName).toString());
			} catch (CorruptedTableException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			
		return res;
	}

	@Override
	public List<List<String>> readColumns(int fromFirstColumnIndex, int toLastColumnIndex) {

		final Table table = getDBFTable();
		
		List<List<String>> res = new ArrayList<>(table.getRecordCount());
		
		final List<String> colNames = table.getFields().subList(fromFirstColumnIndex, toLastColumnIndex).stream().map(f -> f.getName()).collect(Collectors.toList());
		
		for (int i=0; i<table.getRecordCount(); i++)
			try {
				Record record = table.getRecordAt(i);
				res.add(colNames.stream().map(n -> record.getTypedValue(n).toString()).collect(Collectors.toList()) );
			} catch (CorruptedTableException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			
		return res;
	}

	@Override
	public List<String> readColumns(int fromFirstColumnIndex, int toLastColumnIndex, int rowIndex) {
		
		final Table table = getDBFTable();
		final List<String> colNames = table.getFields().subList(fromFirstColumnIndex, toLastColumnIndex).stream().map(f -> f.getName()).collect(Collectors.toList());

		Record record;
		try {
			record = table.getRecordAt(rowIndex);
		} catch (CorruptedTableException | IOException e) {
			throw new RuntimeException(e);
		}
		
		return colNames.stream().map(n -> record.getTypedValue(n).toString()).collect(Collectors.toList());
		

	}

	@Override
	public List<List<String>> readColumns(int fromFirstRowIndex, int toLastRowIndex, int fromFirstColumnIndex,
			int toLastColumnIndex) {
		
		final Table table = getDBFTable();
		
		List<List<String>> res = new ArrayList<>(table.getRecordCount());

		final List<String> colNames = table.getFields().subList(fromFirstColumnIndex, toLastColumnIndex).stream().map(f -> f.getName()).collect(Collectors.toList());

		for (int i=fromFirstRowIndex; i<toLastRowIndex; i++)
			try {
				Record record = table.getRecordAt(i);
				res.add(colNames.stream().map(n -> record.getTypedValue(n).toString()).collect(Collectors.toList()) );
			} catch (CorruptedTableException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			
		return res;
	}

	@Override
	protected void finalize() throws Throwable {
		
		if (table != null) {
		    try {
				table.close();
			} catch (IOException e) {
				e.printStackTrace();
				logger.warn("error while closing the database table "+databaseFilename, e);
			}  
		    table = null;
		}
		
		super.finalize();
	}
	

}
