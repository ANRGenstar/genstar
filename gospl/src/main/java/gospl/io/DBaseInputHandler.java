package gospl.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.metamodel.attribute.demographic.DemographicAttribute;
import core.metamodel.attribute.demographic.DemographicAttributeFactory;
import core.metamodel.io.GSSurveyType;
import core.metamodel.value.IValue;
import core.util.data.GSEnumDataType;
import core.util.excpetion.GSIllegalRangedData;
import nl.knaw.dans.common.dbflib.CorruptedTableException;
import nl.knaw.dans.common.dbflib.Field;
import nl.knaw.dans.common.dbflib.IfNonExistent;
import nl.knaw.dans.common.dbflib.Record;
import nl.knaw.dans.common.dbflib.Table;

/**
 * Handles the DBF DBase INSEE tables.
 * 
 * @see https://en.wikipedia.org/wiki/.dbf
 * 
 * @author Samuel Thiriot
 */
public class DBaseInputHandler extends AbstractInputHandler {

	private static Logger logger = LogManager.getLogger();
	
	private Table table = null;
	private Map<Integer,String> idx2columnName = null;
	
	private GSSurveyType surveyType;
			
	public DBaseInputHandler(GSSurveyType surveyType, String databaseFilename) {

		super(surveyType, databaseFilename);
		
		this.table = null;
		this.idx2columnName = null;
	}
	
	public DBaseInputHandler(GSSurveyType surveyType, File databaseFile) {

		super(surveyType, databaseFile);
		
		this.surveyType = surveyType;
		
		this.table = null;
		this.idx2columnName = null;
	}
	
	protected Table getDBFTable() {

		if (table == null) {
			table = new Table(new File(surveyCompleteFile));
	
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
				throw new IllegalArgumentException("the database "+surveyCompleteFile+" seems corrupted", e);
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException("error reading data from database "+surveyCompleteFile, e);
			} 
			
		}
		return table;
		
	}

	@Override
	public String getName() {
		return new File(surveyCompleteFile).getName();
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
	public Map<Integer, Set<IValue>> getColumnHeaders(Set<DemographicAttribute<? extends IValue>> attributes) {
		
		Map<Integer, Set<IValue>> res = new HashMap<>(attributes.size());
				
		// prepare attributes information
		Map<String,DemographicAttribute<? extends IValue>> name2attribute = attributes.stream()
				.collect(Collectors.toMap(a->a.getAttributeName(), a->a));
		
		// prepare table information
		final Table table = getDBFTable();
		final List<Field> fields = table.getFields();
		
		for (int iField = 0; iField < fields.size(); iField++) {
		
			Field currentField = fields.get(iField);
			
			DemographicAttribute<? extends IValue> att = name2attribute.get(currentField.getName());
			
			if (att == null)
				// ignore missing attributes
				continue;
			
			res.put(iField, att.getValueSpace().stream().collect(Collectors.toSet()));
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
				logger.warn("error while closing the database table "+surveyCompleteFile, e);
			}  
		    table = null;
		}
		
		super.finalize();
	}

	@Override
	public Map<Integer, Set<IValue>> getRowHeaders(Set<DemographicAttribute<? extends IValue>> attributes) {
		return Collections.emptyMap();
	}
	
	/**
	 * WARNING
	 * 
	 * @param f
	 * @param t
	 * @return
	 */
	protected GSEnumDataType getGosplDatatypeForDatabaseType(Field f, Table t) {
		switch (f.getType()) {
		case NUMBER:
			// hard to say... maybe it's integer, maybe not...
			// let's have a look to the table
			if (t.getRecordCount() == 0)
				// well, we have no clue, let's follow theory
				return GSEnumDataType.Integer;
			else {
				String strVal;
				try {
					strVal = t.getRecordAt(0).getTypedValue(f.getName()).toString();
				} catch (CorruptedTableException e1) {
					return GSEnumDataType.Integer;
				} catch (IOException e1) {
					return GSEnumDataType.Integer;
				}
				try {
					Double.parseDouble(strVal);
					return GSEnumDataType.Continue;
				} catch (NumberFormatException e) {
					try {
						Integer.parseInt(strVal);
						return GSEnumDataType.Integer;
					} catch (NumberFormatException e2) {
						return GSEnumDataType.Nominal;
					}
				}
			}
		case FLOAT:
			return GSEnumDataType.Continue;
		case CHARACTER:
		case MEMO:
			return GSEnumDataType.Nominal;
		case LOGICAL:
			return GSEnumDataType.Boolean;
		default:
			throw new IllegalArgumentException();
		}
	}

	@Override
	public Map<Integer, DemographicAttribute<? extends IValue>> getColumnSample(Set<DemographicAttribute<? extends IValue>> attributes) {

		Map<Integer, DemographicAttribute<? extends IValue>> res = new HashMap<>(attributes.size());
				
		// prepare attributes information
		Map<String,DemographicAttribute<? extends IValue>> name2attribute = attributes.stream().collect(Collectors.toMap(a->a.getAttributeName(), a->a));
		
		// prepare table information
		final Table table = getDBFTable();
		final List<Field> fields = table.getFields();
		
		// create an index mapping each index of column with the corresponding attribute
		for (int iField = 0; iField < fields.size(); iField++) {
		
			Field currentField = fields.get(iField);
			
			DemographicAttribute<? extends IValue> att = name2attribute.get(currentField.getName());
			
			if (att == null)
				// ignore missing attributes
				continue;
			
			res.put(iField, att);
			
			// check if we should, in any way, complete the attribute information
			if (att.getValueSpace().getType() == null) {
				// data type is not defined. Let's define it. 
				// what is the datatype in the field ?
				GSEnumDataType dt = null;
				try {
					dt = getGosplDatatypeForDatabaseType(currentField, table);
				} catch (IllegalArgumentException e) {
					logger.warn("unable to automatically define the type for field "+currentField+"; will treat it as a Nominal string value", e);
					dt = GSEnumDataType.Nominal;
				}
				
				// update this attribute !
				logger.info("refining the properties of attribute {} based on database content: its type is now {}", att, dt);

				try {
					DemographicAttribute<? extends IValue> updatedAtt = DemographicAttributeFactory.getFactory()
							.createRefinedAttribute(att, dt);
					attributes.remove(att);
					attributes.add(updatedAtt);
					name2attribute.put(currentField.getName(), updatedAtt);
				} catch (GSIllegalRangedData e) {
					// unable to do that; don't touch this attribute
					logger.warn("error while trying to refine the definition of attribute {} with type {}; leaving it untouched", att, dt); 
					e.printStackTrace();
				}
				
			}
			
		}
		
		return res;
	}
	

}
