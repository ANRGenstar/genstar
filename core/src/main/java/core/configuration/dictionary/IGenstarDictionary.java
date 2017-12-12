package core.configuration.dictionary;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import core.metamodel.attribute.IAttribute;
import core.metamodel.attribute.demographic.MappedDemographicAttribute;
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
    @JsonSubTypes.Type(value = DemographicDictionary.class)
})
public interface IGenstarDictionary<A extends IAttribute<? extends IValue>> {

	/**
	 * Retrieves meaningful attributes describe by this dictionary
	 * 
	 * @see #getRecords()
	 * 
	 * @return
	 */
	public Collection<A> getAttributes();
	
	public boolean containsAttribute(String name);

	/**
	 * Access to attribute using attribute name define as {@link IAttribute#getAttributeName()}
	 * 
	 * @param string
	 * @return
	 */
	public A getAttribute(String string);
	
/**
	 * returns true if one of the attributes of the dictionnary has 
	 * any space containing any value corresponding to this value
	 * @param s
	 * @return
	 */
	public boolean containsValue(String valueStr);
	

	/**
	 * Retrives record attributes contain in this dictionary. Records attribute
	 * are {@link MappedDemographicAttribute} that do not describe any meaningful attribute
	 * for synthetic population but help to parse data. For ex., when we have contingency for
	 * an attribute, it is often coded in data with a corresponding attribute 'population' or
	 * 'frequency'
	 * 
	 * TODO: better description of record
	 * 
	 * @return
	 */
	public Collection<A> getRecords();
	
	/**
	 * Get all attributes in this dictionary
	 * @return
	 */
	@JsonIgnore
	public Collection<A> getAttributesAndRecords();

	/**
	 * Replaces all pre-existing meaningful attributes by the provided collection of attribute.
	 * Ordering of argument collection will be preserve.
	 * 
	 * @param attributes
	 */
	public void setAttributes(Collection<A> attributes);
	
	/**
	 * Replaces all pre-existing record attributes by the provided collection of attribute.
	 * Ordering of argument collection is not guaranteed to be preserve.
	 * 
	 * @param attributes
	 */
	public void setRecords(Collection<A> attributes);

	@SuppressWarnings("unchecked")
	public IGenstarDictionary<A> addAttributes(A... attributes);
	
	public IGenstarDictionary<A> addAttributes(Collection<A> attributes);

	/**
	 * Add record attributes to this dictionary.
	 * 
	 * @param records
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public IGenstarDictionary<A> addRecords(A... records);


	/**
	 * add the dictionnary passed as parameter to this one and returns 
	 * a novel dictionnary which contains all their attributes.
	 * @param dictionnary
	 * @return
	 */
	public IGenstarDictionary<A> merge(IGenstarDictionary<A> dictionnary);

	/**
	 * returns the count of attributes
	 * @return
	 */
	public int size();
}
