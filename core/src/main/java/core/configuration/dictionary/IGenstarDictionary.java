package core.configuration.dictionary;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import core.metamodel.attribute.EmergentAttribute;
import core.metamodel.attribute.IAttribute;
import core.metamodel.attribute.record.RecordAttribute;
import core.metamodel.entity.IEntity;
import core.metamodel.value.IValue;

/**
 * Encapsulate description of attributes that will characterized synthetic population entities and help
 * to parse input data
 * 
 * 
 * To read such a dictionnary from file, refer to ReadINSEEDictionaryUtils in the gospl package.
 * 
 * @author Kevin Chapuis
 * @author Samuel Thiriot
 *
 * @param <A>
 */
@JsonTypeInfo(
	      use = JsonTypeInfo.Id.NAME,
	      include = JsonTypeInfo.As.WRAPPER_OBJECT
	      )
@JsonSubTypes({
    @JsonSubTypes.Type(value = AttributeDictionary.class)
})
public interface IGenstarDictionary<A extends IAttribute<? extends IValue>> {

	public static final String ATTRIBUTES = "ATTRIBUTES";
	public static final String RECORDS = "RECORDS";
	public static final String SIZE = "SIZE ATTRIBUTE";
	
	/**
	 * Retrieves meaningful attributes describe by this dictionary
	 * 
	 * @see #getRecords()
	 * 
	 * @return
	 */
	@JsonProperty(AttributeDictionary.ATTRIBUTES)
	public Collection<A> getAttributes();
	
	@JsonIgnore
	public Collection<IAttribute<? extends IValue>> getAttributeAndRecord();
	
	/**
	 * Access to attribute using attribute name define as {@link IAttribute#getAttributeName()}
	 * 
	 * @param string
	 * @return
	 */
	public A getAttribute(String attribute);
	
	/**
	 * Access to value 
	 * 
	 * @param value
	 * @return
	 */
	public IValue getValue(String value);
	
	/**
	 * true if this dictionary contains an attribute associated to the attribute name passed as argument; 
	 * false otherwise
	 * 
	 * @param name
	 * @return
	 */
	public boolean containsAttribute(String name);
	
	/**
	 * true if this dictionary contains a record attribute associated to attribute name passed as argument; 
	 * false otherwise
	 * 
	 * @param name
	 * @return
	 */
	public boolean containsRecord(String name);
	
	/**
	 * returns true if one of the attributes of the dictionnary has 
	 * any space containing any value corresponding to this value
	 * @param s
	 * @return
	 */
	public boolean containsValue(String valueStr);

	// ---------------------- ADD & SET

	@SuppressWarnings("unchecked")
	public IGenstarDictionary<A> addAttributes(A... attributes);
	
	public IGenstarDictionary<A> addAttributes(Collection<A> attributes);
	
	// ------------------------ RECORDS
	
	/**
	 * Retrives record attributes contain in this dictionary.
	 * 
	 * TODO: better description of record
	 * 
	 * @return
	 */
	@JsonProperty(AttributeDictionary.RECORDS)
	public Collection<RecordAttribute<A, A>> getRecords();
	
	/**
	 * Add record attributes to this dictionary.
	 * 
	 * @param records
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public IGenstarDictionary<A> addRecords(RecordAttribute<A, A>... records);
	
	//public void setRecords(Collection<RecordAttribute<A, A>> records);
	
	// ------------------------ SIZE
	
	@JsonProperty(AttributeDictionary.SIZE)
	public EmergentAttribute<? extends IValue, Collection<IEntity<? extends IAttribute<? extends IValue>>>, ?> getSizeAttribute();
	
	// ----------- UTILITIES

	/**
	 * returns the count of attributes
	 * @return
	 */
	public int size();
	
	/**
	 * add the dictionary passed as parameter to this one and returns 
	 * a novel dictionary which contains all their attributes.
	 * @param dictionnary
	 * @return
	 */
	public IGenstarDictionary<A> merge(IGenstarDictionary<A> dictionnary);

}
