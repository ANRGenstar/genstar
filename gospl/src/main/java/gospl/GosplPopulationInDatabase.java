package gospl;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.formula.eval.NotImplementedException;

import core.metamodel.IPopulation;
import core.metamodel.attribute.demographic.DemographicAttribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.entity.EntityUniqueId;
import core.metamodel.value.IValue;
import core.metamodel.value.binary.BinarySpace;
import core.metamodel.value.binary.BooleanValue;

/**
 * Stores a population in database; provides quick access. 
 * 
 * TODO prepared statements for perf
 * 
 * @author Samuel Thiriot
 */
public class GosplPopulationInDatabase 
	implements IPopulation<ADemoEntity, DemographicAttribute<? extends IValue>> {

	public static final int VARCHAR_SIZE = 255;
	public static final int MAX_BUFFER_QRY = 10000;
	public static final String DEFAULT_ENTITY_TYPE = "unknown";
	public static int REMOVE_ENTITIES_BATCH = 100;
	public static int ADD_ENTITIES_BATCH = 500; // TODO more !
	
	private Logger logger = LogManager.getLogger();
	
	private final Connection connection;

	private Map<String,String> entityType2tableName = new HashMap<>();
	private Map<String,Map<DemographicAttribute<? extends IValue>,String>> entityType2attribute2colName = new HashMap<>();

	private Map<String,Set<DemographicAttribute<? extends IValue>>> entityType2attributes = new HashMap<>();
	
	private static int currentInstanceCount = 0;
	
	private String mySqlDBname = "GosplPopulation_"+(++currentInstanceCount);
	
	public GosplPopulationInDatabase(Connection connection, GosplPopulation population) {
		this.connection = connection;
		loadPopulationIntoDatabase(population);
	} 

	/**
	 * Creates an empty population in memory
	 */
	public GosplPopulationInDatabase() {
		try {
			this.connection = DriverManager.getConnection("jdbc:hsqldb:mem:"+mySqlDBname+";shutdown=true", "SA", "");
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("error while trying to initialize the HDSQL database engine in memory: "+e.getMessage(), e);
		}
	} 
	
	/**
	 * Creates a population stored in memory. 
	 * Suitable as long as the population is not too big. 
	 * @param population
	 * @param connection
	 */
	public GosplPopulationInDatabase(GosplPopulation population) {
		try {
			this.connection = DriverManager.getConnection("jdbc:hsqldb:mem:"+mySqlDBname+";shutdown=true", "SA", "");
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("error while trying to initialize the HDSQL database engine in memory: "+e.getMessage(), e);
		}
		loadPopulationIntoDatabase(population);
	} 

	/**
	 * Stores the population in a file passed as parameter.
	 * @param databaseFile
	 * @param population
	 */
	public GosplPopulationInDatabase(File databaseFile, GosplPopulation population) {
		
		 try {
			 //;ifexists=true
			this.connection = DriverManager.getConnection("jdbc:hsqldb:file:"+databaseFile.getPath()+";create=true;shutdown=true;", "SA", "");
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("error while trying to initialize the HDSQL database engine in file "
								+databaseFile+": "+e.getMessage(), e);
		}
		 loadPopulationIntoDatabase(population);
	}
	
	/**
	 * The connection might be "hsql://localhost/xdb" or in the form "http://localhost/xdb".
	 * @see http://hsqldb.org/doc/2.0/guide/running-chapt.html#N100CF
	 * @param hsqlUrlServer
	 * @param pop
	 */
	public GosplPopulationInDatabase(URL hsqlUrlServer, GosplPopulation population) {
		try {
		     Class.forName("org.hsqldb.jdbc.JDBCDriver" );
		} catch (Exception e) {
		     System.err.println("ERROR: failed to load HSQLDB JDBC driver.");
		     e.printStackTrace();
			throw new RuntimeException("error while trying to load the JDBC driver to load the HSQL database", e);
		}

		try {
			this.connection = DriverManager.getConnection("jdbc:hsqldb:"+hsqlUrlServer+";shutdown=true", "SA", "");
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("error while trying to initialize the HDSQL database engine in file "
					+hsqlUrlServer+": "+e.getMessage(), e);
		}
		loadPopulationIntoDatabase(population);
	}
	
	
	protected String getTableNameForEntityType(String type) {
		String tableName = entityType2tableName.get(type);
		if (tableName == null) {
			tableName = "entities_"+type;
			entityType2tableName.put(type, tableName);
		}
		return tableName;
	}
	
	protected String getAttributeColNameForType(String type, DemographicAttribute<? extends IValue> a) {
		Map<DemographicAttribute<? extends IValue>,String> a2name = entityType2attribute2colName.get(type);
		if (a2name == null) {
			a2name = new HashMap<>();
			entityType2attribute2colName.put(type, a2name);
		}
		String colName = a2name.get(a);
		if (colName == null) {
			colName = a.getAttributeName().replaceAll("(\\W|^_)*", "");
			a2name.put(a, colName);
		}
		return colName;
	}
	
	protected String getSQLTypeForAttribute(DemographicAttribute<? extends IValue> a) {
		
		switch (a.getValueSpace().getType()) {
		case Integer:
			return "INTEGER"; 
		case Continue:
			return "DOUBLE";
		case Nominal:
		case Order:
		case Range:
			return "VARCHAR("+VARCHAR_SIZE+")";
		case Boolean:
			return "BOOLEAN";
		default:
			new RuntimeException("this attribute type is not managed: "+a.getValueSpace().getType());
		}
		
		// can never reach here
		return "HELP";
	}
	
	protected void createTableForEntityType(String type) throws SQLException {
		
		// prepare the SQL query
		StringBuffer sb = new StringBuffer();
		sb.append("CREATE TABLE ") // LOCAL TEMPORARY 
			.append(getTableNameForEntityType(type))
			.append(" (");
		
		sb.append("id VARCHAR(50) PRIMARY KEY"); // TODO maybe 30 is not enough?
		
		for (DemographicAttribute<? extends IValue> a: entityType2attributes.get(type)) {
			sb.append(", ");
			
			// colname
			sb.append(getAttributeColNameForType(type, a));
			sb.append(" ");
			
			// type
			sb.append(getSQLTypeForAttribute(a));
			sb.append(" ");
			
		}
		sb.append(")");
		final String qry = sb.toString();
		
		// execute 
		logger.info("creating table for type {} with SQL query: {}", type, qry);
		Statement s = connection.createStatement();				
		s.execute(qry);
		s.close();
	}
	
	/**
	 * creates a different table for each entity type 
	 * and defines the corresponding attributes.
	 * @throws SQLException
	 */
	protected void createInitialTables() throws SQLException {
	
		// create one table per type with the corresponding attributes
		for (String type: entityType2attributes.keySet()) {
			createTableForEntityType(type);
		}
		
		// TODO indexes !
		
	}
	
	
	/**
	 * Loads the given collection: keeps in memory the attributes,
	 * create tables, and loads entities into them.
	 * @param pop
	 */
	protected void loadPopulationIntoDatabase(GosplPopulation pop) {
		assert this.connection != null;
		assert pop != null;
		
		// create the attributes 
		// we don't know the entity type for this population
		String entityType = DEFAULT_ENTITY_TYPE;
		this.entityType2attributes.put(entityType, new HashSet<>(pop.getPopulationAttributes()));
		
		// create internal structure
		try {
			createInitialTables();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("error creating the tables to store the population in database: "+e.getMessage(), e);
		}
		
		try {
			storeEntities(entityType, pop);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("error while inserting the population in database: "+e.getMessage(), e);
			
		}
	}

	/**
	 * Returns the SQL value for the attribute of an entity
	 * @param e
	 * @param a
	 * @return
	 */
	private String getSQLValueFor(ADemoEntity e, DemographicAttribute<? extends IValue> a) {
		
		IValue v = e.getValueForAttribute(a);
		
		switch (a.getValueSpace().getType()) {
			case Continue:
			case Integer:
				return v.getStringValue();
			case Nominal:
			case Order:
			case Range:
				return "'"+v.getStringValue()+"'";
			case Boolean:
				return ((BooleanValue)v).getActualValue()?"TRUE":"FALSE";
			default:
				throw new RuntimeException("unknown value type "+a.getValueSpace().getType());
		}
	
	}
	
	/**
	 * For a given attribute of an entity of a given type, decodes the value from a SQL resultset 
	 * and returns the corresponding genstar value.
	 * @param type
	 * @param a
	 * @param r
	 * @return
	 * @throws SQLException 
	 */
	protected IValue readValueForAttribute(String type, DemographicAttribute<? extends IValue> a, ResultSet r) throws SQLException {
		final String colName = getAttributeColNameForType(type, a);
		switch (a.getValueSpace().getType()) {
		case Integer:
			int valueInd = r.getInt(colName);
			return a.getValueSpace().getValue(Integer.toString(valueInd));
		case Continue:
			double valueDouble = r.getDouble(colName);
			return a.getValueSpace().getValue(Double.toString(valueDouble));
		case Nominal:
		case Range:
		case Order:
			String valueStr = r.getString(colName);
			return a.getValueSpace().getValue(valueStr);
		case Boolean:
			if (r.getBoolean(colName)) {
				return ((BinarySpace)a.getValueSpace()).valueTrue;
			} else {
				return ((BinarySpace)a.getValueSpace()).valueFalse;
			}
		default :
			throw new RuntimeException("unknown entity type "+a.getValueSpace().getType());
		}
	}
	
	
	protected int storeEntities(String type, Collection<? extends ADemoEntity> entities) throws SQLException {

		if (!entityType2tableName.containsKey(type))
			createTableForEntityType(type);
		
		int added = 0;
		
		// name columns 
		List<DemographicAttribute<? extends IValue>> attributes = new LinkedList<>(entityType2attributes.get(type));
		
		StringBuffer sb = new StringBuffer();
		sb.append("INSERT INTO ").append(getTableNameForEntityType(type));
		sb.append(" m (id");
		for (DemographicAttribute<? extends IValue> a: attributes) {
			sb.append(",");
			sb.append(getAttributeColNameForType(type, a));
		}
		sb.append(") VALUES (");
		final String qryHead = sb.toString();
		
		// add each entity
		boolean first = true;
		for (ADemoEntity e: entities) {
			
			if (sb.length() >= MAX_BUFFER_QRY) {
				sb.append(")");
				final String qry = sb.toString();
				// execute the query
				logger.info("adding entities with query {}", qry);
				Statement st = connection.createStatement();
				st.executeQuery(qry);
				ResultSet rs = st.executeQuery("CALL DIAGNOSTICS ( ROW_COUNT )");
				rs.next();
				added += rs.getInt(1);
				rs.close();
				st.close();
				// restart from scratch 
				first = true;
				sb = new StringBuffer(qryHead);
			}
			
			if (first) {
				first = false;
			} else {
				sb.append(",");
			}
			sb.append("('");
			sb.append(e.getEntityId());
			sb.append("'");
			for (DemographicAttribute<? extends IValue> a: attributes) {
				sb.append(",");
				sb.append(getSQLValueFor(e,a));
			}
			sb.append(")");
			
		}
		
		if (sb.length() > qryHead.length()) {
			sb.append(")");
			final String qry = sb.toString();
			// execute the query
			logger.info("adding last entities with query {}", qry);
			Statement st = connection.createStatement();
			st.executeQuery(qry);
			ResultSet rs = st.executeQuery("CALL DIAGNOSTICS ( ROW_COUNT )");
			rs.next();
			added += rs.getInt(1);
			rs.close();
			st.close();
		}
		return added;
	}

	@Override
	public boolean add(ADemoEntity e) {

		String type = e.getEntityType();
		if (type == null)
			type = DEFAULT_ENTITY_TYPE;
			
		if (!entityType2attributes.containsKey(type))
			entityType2attributes.put(type, new HashSet<>(e.getAttributes()));
		
		if (!entityType2tableName.containsKey(type)) {
			try {
				createTableForEntityType(type);
			} catch (SQLException ex) {
				throw new RuntimeException("error while creating table for type "+type);
			}
		}
		
		// name columns 
		List<DemographicAttribute<? extends IValue>> attributes = 
				new LinkedList<>(entityType2attributes.get(type));
		
		StringBuffer sb = new StringBuffer();
		sb.append("INSERT INTO ").append(getTableNameForEntityType(type));
		sb.append(" (id");
		for (DemographicAttribute<? extends IValue> a: attributes) {
			sb.append(",");
			sb.append(getAttributeColNameForType(type, a));
		}
		sb.append(") VALUES");
		
		sb.append("('");
		sb.append(e.getEntityId());
		sb.append("'");
		for (DemographicAttribute<? extends IValue> a: attributes) {
			sb.append(",");
			sb.append(getSQLValueFor(e,a));
		}
		sb.append(")");
		
		boolean anyChange = false;
		try {
			Statement st = connection.createStatement();
			st.executeQuery(sb.toString());
			st.close();
			return true;
		} catch(SQLIntegrityConstraintViolationException e1) {
			return false;
		} catch (SQLException e1) {
			e1.printStackTrace();
			throw new RuntimeException("error while adding entity "+e, e1);
		}
		
	}

	@Override
	public boolean addAll(Collection<? extends ADemoEntity> c) {
		
		int added = 0;
		
		Map<String,List<ADemoEntity>> type2entities = new HashMap<>();
		
		try {
				
			for (Object o: c) {
				ADemoEntity e = null;
				try {
					e = (ADemoEntity)o;
				} catch (ClassCastException e1) {
					// skip was is not an entity
					continue;
				}
				
				String type = e.getEntityType();
				if (type == null)
					type = DEFAULT_ENTITY_TYPE;
				
				if (!e._hasEntityId())
					e._setEntityId(EntityUniqueId.createNextId(this, type));
				
				System.out.println("should add entity id: "+e.getEntityId());

				if (!entityType2attributes.containsKey(type))
					entityType2attributes.put(type, new HashSet<>(e.getAttributes()));
				
				if (!entityType2tableName.containsKey(type)) {
					try {
						createTableForEntityType(type);
					} catch (SQLException ex) {
						throw new RuntimeException("error while creating table for type "+type);
					}
				}
				
				List<ADemoEntity> l = type2entities.get(type);
				if (l == null) { 
					l = new ArrayList<>(ADD_ENTITIES_BATCH);
					type2entities.put(type, l);
				}
				l.add(e);
				
				if (l.size() >= ADD_ENTITIES_BATCH) {
					try {
						added += storeEntities(type, l);
					} catch (SQLIntegrityConstraintViolationException e2) {
						// one of the entities already exist; 
						// there is no easy and efficient synthax for sqldb to exclude the known ones
						// the best is to just add them one by one
						System.err.println("some of these agents already existed; switching to add 1 by 1");
						for (ADemoEntity en: l) {
							if (add(en))
								added++;
						}
					}
					l.clear();
				}
			}
			
			for (String type: type2entities.keySet()) {
			
				try {
					added += storeEntities(type, type2entities.get(type));
				} catch (SQLIntegrityConstraintViolationException e2) {
					// one of the entities already exist; 
					// there is no easy and efficient synthax for sqldb to exclude the known ones
					// the best is to just add them one by one
					System.err.println("some of these agents already existed; switching to add 1 by 1");
					for (ADemoEntity en: type2entities.get(type)) {
						if (add(en))
							added++;
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("error while adding entities",e);
		}
		return added > 0;
	
	}

	@Override
	public void clear() {
		try {
			Statement st = connection.createStatement();
			for (String tablename : entityType2tableName.values()) {
				st.executeQuery("TRUNCATE TABLE "+tablename);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("error while dropping the table containing the entities", e);
		}
	}

	@Override
	public boolean contains(Object o) {
		if (o instanceof ADemoEntity) {
			ADemoEntity e = (ADemoEntity)o;
			String entityType = e.getEntityType();
			if (entityType == null)
				entityType = DEFAULT_ENTITY_TYPE;
			String tableName = entityType2tableName.get(entityType);
			if (tableName == null)
				// we never saw such a type; we cannot contain it
				return false;
			try {
				Statement st = connection.createStatement();
				ResultSet set = st.executeQuery("SELECT COUNT(*) FROM "+tableName+" WHERE id='"+e.getEntityId()+"'");
				set.next();
				Integer count = set.getInt(0);
				return count > 0;
			} catch (SQLException e1) {
				e1.printStackTrace();
				throw new RuntimeException("Unable to search for entity "+o,e1);
			}
		}
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		
		// TODO implement that, we can do it (but its tedious and no one ever uses it !)
		throw new NotImplementedException("Not yet implemented"); 
	}

	@Override
	public boolean isEmpty() {
		
		// easy solution: no entity type means nothing was ever inserted
		if (entityType2tableName.isEmpty())
			return true;
		
		// the hard way 
		try {
			Statement st = connection.createStatement(
					ResultSet.TYPE_SCROLL_INSENSITIVE,
		            ResultSet.CONCUR_READ_ONLY);
			
			for (String tableName: entityType2tableName.values()) {
				//ResultSet set = st.executeQuery("SELECT * FROM "+tableName+";");
				
				ResultSet set = st.executeQuery("SELECT * FROM "+tableName+" LIMIT 1");
				
				if (!set.next())
					return true;
				
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
			throw new RuntimeException("Error while checking if the table is empty",e1);
		}
		
		return false;
	}

	/**
	 * Iterates the entities of a given type
	 * 
	 * @author Samuel Thiriot
	 */
	public class DatabaseEntitiesIterator implements Iterator<ADemoEntity> {

	    private ResultSet rs;
	    private PreparedStatement ps;
	    private Connection connection;
	    private String sql;
	    private String type;
	    private Set<DemographicAttribute<? extends IValue>> attributes;
	    
	    public DatabaseEntitiesIterator(Connection connection, Set<DemographicAttribute<? extends IValue>> attributes, String type, String sql) {
	        assert connection != null;
	        assert sql != null;
	        assert type != null;
	        assert attributes != null;
	        this.connection = connection;
	        this.sql = sql;
	        this.attributes = attributes;
	        this.type = type;
	    }

	    /**
	     * Creates an iterator browsing all the entities of this type
	     * @param connection
	     * @param type
	     */
	    public DatabaseEntitiesIterator(Connection connection, Set<DemographicAttribute<? extends IValue>> attributes, String type) {
	    	this(connection, attributes, type, "SELECT * FROM "+entityType2tableName.get(type));
	    }
	    
	    public void init() {
	        try {
	            ps = connection.prepareStatement(sql);
	            rs = ps.executeQuery();
	            rs.next();
	        } catch (SQLException e) {
	            close();
	            throw new RuntimeException();
	        }
	    }

	    @Override
	    public boolean hasNext() {
	        if (ps == null) {
	            init();
	        }
	        try {
	            boolean hasMore = !rs.isAfterLast();
	            // TODO avoid this ugly workaround ?!
	            if (hasMore) {
	            	try {
		            	rs.getString("id");
		            } catch (SQLException e) {
		            	hasMore = false;
		            }
	            }
	            if (!hasMore) {
	                close();
	            }
	            return hasMore;
	        } catch (SQLException e) {
	            close();
	            throw new RuntimeException(e);
	        }

	    }

	    private void close() {
	        try {
	            rs.close();
	            try {
	                ps.close();
	            } catch (SQLException e) {
	                //nothing we can do here
	            }
	        } catch (SQLException e) {
	            //nothing we can do here
	        }
	    }

	    @Override
	    public ADemoEntity next() {
	        
	    	Map<DemographicAttribute<? extends IValue>,IValue> attribute2value = new HashMap<>();
	    	
	    	// read the attributes of the current element 
	    	String id;
	    	try {
	    		id = rs.getString("id");
	    	} catch (SQLException e) {
				e.printStackTrace();
				throw new RuntimeException("error while reading the id from database: "+e.getMessage(), e);
			}
	    	for (DemographicAttribute<? extends IValue> a: attributes) {
	    		try {
					attribute2value.put(a, readValueForAttribute(type, a, rs));
				} catch (SQLException e) {
					e.printStackTrace();
					throw new RuntimeException("error while reading the value "+a+" from database: "+e.getMessage(), e);
				}
	        } 
	    	
    		try {
    			rs.next();
    		} catch (SQLException e) {
    			throw new RuntimeException("error while going to the next record: "+e.getMessage(), e);
			}

	    	// create the return result
	    	GosplEntity res = new GosplEntity(attribute2value);
	    	res._setEntityId(id);
	    	res.setEntityType(type);
	    	
	    	return res;
	    }
	}
	

	/**
	 * Iterates the entities of a all types
	 * 
	 * @author Samuel Thiriot
	 */
	public class AllTypesIterator implements Iterator<ADemoEntity> {

	    private Connection connection;
		private Map<String,Set<DemographicAttribute<? extends IValue>>> entityType2attributes;
		
		private Iterator<String> itTypes = null;
		private DatabaseEntitiesIterator itEntities = null;
		
		private String currentType = null;
		
		//TODO add SQL selector

	    public AllTypesIterator(
	    		Connection connection, 
	    		Map<String,String> entityType2tableName,
	    		Map<String,Set<DemographicAttribute<? extends IValue>>> entityType2attributes
	    		) {
	        assert connection != null;
	        
	        this.connection = connection;
	        this.entityType2attributes = entityType2attributes;
	        
	        itTypes = entityType2tableName.keySet().iterator();
	        itTypes.hasNext();
	    }

	    protected void initEntitiesIterator() {
    		String currentType = itTypes.next();
    		itEntities = new DatabaseEntitiesIterator(
    				connection, 
    				entityType2attributes.get(currentType), 
    				currentType
    				);
	    }
	    @Override
	    public boolean hasNext() {
	    	
	        if (itEntities == null)
	        	initEntitiesIterator();
	        	
	        if (!itEntities.hasNext()) {
	        	System.out.println("end of the entities iterator");
	        	return itTypes.hasNext();
	        } else {
	        	return true;
	        }
	    }

	    @Override
	    public ADemoEntity next() {
	        
	    	/*if (itEntities == null || !itEntities.hasNext()) {
	    		currentType = itTypes.next();
	    		
	    	}*/
	    	//itTypes.hasNext()
	    	if (itEntities == null || !itEntities.hasNext()) 
	    		initEntitiesIterator();
	    	
	    	return itEntities.next();
	    }
	}
	
	
	@Override
	public Iterator<ADemoEntity> iterator() {
		
		return new AllTypesIterator(connection, entityType2tableName, entityType2attributes);
	}
	
	public Iterator<ADemoEntity> iterator(String type) {
		return new DatabaseEntitiesIterator(connection, entityType2attributes.get(type), type);
	}

	@Override
	public boolean remove(Object o) {
		try {
			ADemoEntity e = (ADemoEntity)o;
			
			if (!entityType2tableName.containsKey(e.getEntityType()))
				// if we hold no agent of this type, we did not delete it 
				return false;
			
			if (!e._hasEntityId())
				// we never stored an agent without id 
				return false;
			
			try {
				Statement st = connection.createStatement();
				st.executeQuery("DELETE FROM "+
						getTableNameForEntityType(e.getEntityType())+
						" WHERE id='"+e.getEntityId()+"'"
						);
				
				// check if we deleted anything
				ResultSet rs = st.executeQuery("CALL DIAGNOSTICS ( ROW_COUNT )");
				rs.next();
				return rs.getInt(1) > 0;
			} catch (SQLException ex) {
				ex.printStackTrace();
				throw new RuntimeException("SQL error while deleting the entity "+e, ex);
			}
			
		} catch (ClassCastException e1) {
			// if this is not an entity, we did not removed it
			return false;
		}
	}

	protected void createIdsClause(StringBuffer sb, Collection<String> ids) {
		sb.append("IN (");
		boolean first = true;
		for (String id : ids) {
			if (first)
				first = false;
			else 
				sb.append(",");
			sb.append("'").append(id).append("'");
		}
		sb.append(")");
		
	}
	
	protected int deleteIds(String type, Collection<String> ids) {
		StringBuffer sb = new StringBuffer();
		sb.append("DELETE FROM ").append(getTableNameForEntityType(type)).append(" WHERE id ");
		createIdsClause(sb, ids);
		
		try {
			Statement st = connection.createStatement();
			st.executeQuery(sb.toString());
			// check if we deleted anything
			ResultSet rs = st.executeQuery("CALL DIAGNOSTICS ( ROW_COUNT )");
			rs.next();
			return rs.getInt(1);
		} catch (SQLException ex) {
			ex.printStackTrace();
			throw new RuntimeException("SQL error while deleting the entities "+ex, ex);
		}
	}
	
	@Override
	public boolean removeAll(Collection<?> c) {

		
		boolean anyChange = false;
		
		Map<String,List<String>> type2ids = new HashMap<>();
		for (Object o: c) {
			ADemoEntity e = null;
			try {
				e = (ADemoEntity)o;
			} catch (ClassCastException e1) {
				// skip was is not an entity
				continue;
			}
			if (!e._hasEntityId())
				continue;
			
			String type = e.getEntityType();
			if (type == null)
				type = DEFAULT_ENTITY_TYPE;
			
			if (!entityType2tableName.containsKey(type))
				// we never saved it, so we will not remove it
				continue;
			
			List<String> l = type2ids.get(type);
			if (l == null) { 
				l = new ArrayList<>(REMOVE_ENTITIES_BATCH);
				type2ids.put(type, l);
			}
			l.add(e.getEntityId());
			
			if (l.size() >= REMOVE_ENTITIES_BATCH) {
				anyChange = deleteIds(type, l)>0 || anyChange;
				l.clear();
			}
		}
		
		for (String type: type2ids.keySet()) {
			anyChange = deleteIds(type, type2ids.get(type)) > 0 || anyChange;
		}
		return anyChange;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
	
		throw new NotImplementedException("cannot retain all for all the types");
	}

	@Override
	public int size() {
		try {
			int accumulated = 0;
			System.out.println("in size");
			Statement st = connection.createStatement();
			
			for (String tableName: entityType2tableName.values()) {
				
				ResultSet set = st.executeQuery("SELECT COUNT(*) as TOTAL FROM "+tableName+";");
				set.next();
				accumulated += set.getInt("TOTAL");
			}
			return accumulated;
		} catch (SQLException e1) {
			e1.printStackTrace();
			throw new RuntimeException("Error while counting entities",e1);
		}
	}

	@Override
	public Object[] toArray() {
		throw new NotImplementedException("no array feature for this");
	}

	@Override
	public <T> T[] toArray(T[] a) {
		throw new NotImplementedException("no array feature for this");
	}

	@Override
	public Set<DemographicAttribute<? extends IValue>> getPopulationAttributes() {
		return entityType2attributes.values().stream()
				.flatMap(coll -> coll.stream())
				.collect(Collectors.toSet());
	}

	@Override
	public boolean isAllPopulationOfType(String type) {
		return entityType2tableName.size() == 1 && entityType2tableName.containsKey(type);
	}

	@Override
	public DemographicAttribute<? extends IValue> getPopulationAttributeNamed(String name) {
		// TODO index
		Set<DemographicAttribute<? extends IValue>> attributes = getPopulationAttributes();
		if (attributes == null)
			return null;
		for (DemographicAttribute<? extends IValue> a: attributes) {
			if (a.getAttributeName().equals(name))
				return a;
		}
		return null;
	}
	
	/**
	 * At finalization time, we shutdown the database
	 */
	@Override
	protected void finalize() throws Throwable {
		
		if (this.connection != null) {
			this.connection.close();		
		}
		super.finalize();
	}

}
