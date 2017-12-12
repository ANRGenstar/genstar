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
 * @author kevinchapuis
 * @author samthiriot
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
	
	/**
	 * Access to attribute using attribute name define as {@link IAttribute#getAttributeName()}
	 * 
	 * @param string
	 * @return
	 */
	public A getAttribute(String string);
	
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

	/**
	 * Add a meaningful attribute to this dictionary. Insertion order is preserved.
	 * 
	 * @param attributes
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public IGenstarDictionary<A> addAttributes(A... attributes);
	
	/**
	 * Add record attributes to this dictionary.
	 * 
	 * @param records
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public IGenstarDictionary<A> addRecords(A... records);

	/**
	 * returns the count of attributes
	 * @return
	 */
	public int size();
}
