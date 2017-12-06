package gospl;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
import org.bouncycastle.crypto.RuntimeCryptoException;

import core.metamodel.IPopulation;
import core.metamodel.attribute.demographic.DemographicAttribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.entity.EntityUniqueId;
import core.metamodel.value.IValue;
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

	private Logger logger = LogManager.getLogger();
	
	private final Connection connection;

	private Map<String,String> entityType2tableName = new HashMap<>();
	private Map<String,Map<DemographicAttribute<? extends IValue>,String>> entityType2attribute2colName = new HashMap<>();

	private Map<String,Set<DemographicAttribute<? extends IValue>>> entityType2attributes = new HashMap<>();
	
	
	public GosplPopulationInDatabase(Connection connection, GosplPopulation population) {
		this.connection = connection;
		loadPopulationIntoDatabase(population);
	} 

	/**
	 * Creates a population stored in memory. 
	 * Suitable as long as the population is not too big. 
	 * @param population
	 * @param connection
	 */
	public GosplPopulationInDatabase(GosplPopulation population) {
		try {
			this.connection = DriverManager.getConnection("jdbc:hsqldb:mem:mymemdb;shutdown=true", "SA", "");
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
			this.connection = DriverManager.getConnection("jdbc:hsqldb:file:"+databaseFile.getPath()+";shutdown=true;ifexists=true", "SA", "");
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
		sb.append("DECLARE LOCAL TEMPORARY TABLE ")
			.append(getTableNameForEntityType(type))
			.append(" (");
		
		sb.append("id VARCHAR(30) PRIMARY KEY"); // TODO maybe 30 is not enough?
		
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
	
	protected void storeEntities(String type, Collection<? extends ADemoEntity> entities) throws SQLException {

		// name columns 
		List<DemographicAttribute<? extends IValue>> attributes = new LinkedList<>(entityType2attributes.get(type));
		
		StringBuffer sb = new StringBuffer();
		sb.append("INSERT INTO ").append(getTableNameForEntityType(type));
		sb.append(" (id");
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
			sb.append(EntityUniqueId.createNextId(this, type));
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
		}
	}

	@Override
	public boolean add(ADemoEntity e) {

		String type = e.getEntityType();
		if (type == null)
			type = DEFAULT_ENTITY_TYPE;
			
		// name columns 
		List<DemographicAttribute<? extends IValue>> attributes = new LinkedList<>(entityType2attributes.get(type));
		
		StringBuffer sb = new StringBuffer();
		sb.append("INSERT INTO ").append(getTableNameForEntityType(type));
		sb.append(" (id");
		for (DemographicAttribute<? extends IValue> a: attributes) {
			sb.append(",");
			sb.append(getAttributeColNameForType(type, a));
		}
		sb.append(") VALUES (");
		
		sb.append("('");
		sb.append(EntityUniqueId.createNextId(this, type));
		sb.append("'");
		for (DemographicAttribute<? extends IValue> a: attributes) {
			sb.append(",");
			sb.append(getSQLValueFor(e,a));
		}
		sb.append(");");
		
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends ADemoEntity> c) {
		try {
			storeEntities("unknown",c);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("error while inserting the population in database: "+e.getMessage(), e);
		}
		return true;
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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEmpty() {
		if (entityType2tableName.isEmpty())
			return true;
		// TODO
		return false;
	}

	@Override
	public Iterator<ADemoEntity> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean remove(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int size() {
		try {
			int accumulated = 0;
			System.out.println("in size");
			Statement st = connection.createStatement();
			for (String tableName: entityType2tableName.values()) {
				ResultSet set = st.executeQuery("SELECT COUNT(*) AS total FROM "+tableName+";");
				System.err.println("SELECT COUNT(*) AS count FROM "+tableName+";");
				set.next();
				System.out.println(set.getString("total"));
				accumulated += set.getInt(1);	
			}
			return accumulated;
		} catch (SQLException e1) {
			e1.printStackTrace();
			throw new RuntimeException("Error while counting entities",e1);
		}
	}

	@Override
	public Object[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T[] toArray(T[] a) {
		// TODO Auto-generated method stub
		return null;
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

	/**
	 * At finalization time, we shutdown the database
	 */
	@Override
	protected void finalize() throws Throwable {
		// TODO SHUTDOWN
		super.finalize();
	}

}
