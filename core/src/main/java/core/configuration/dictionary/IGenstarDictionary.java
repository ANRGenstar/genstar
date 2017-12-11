package core.configuration.dictionary;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import core.metamodel.attribute.IAttribute;
import core.metamodel.value.IValue;

/**
 * 
 * Describes a dictionnary of data, that is the metadata which explains how to decode data 
 * encoded in a file or database. 
 * 
 * To read such a dictionnary from file, refer to ReadINSEEDictionaryUtils in the gospl package.
 * 
 * @author Kevin Chapuis
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

	public Collection<A> getAttributes();
	
	/**
	 * Returns the attribute for this name
	 * @param string
	 * @return
	 */
	public A getAttribute(String string);

	public void setAttributes(Collection<A> attributes);

	public boolean containsAttribute(String name);
	
	/**
	 * returns true if one of the attributes of the dictionnary has 
	 * any space containing any value corresponding to this value
	 * @param s
	 * @return
	 */
	public boolean containsValue(String valueStr);
	
	/**
	 * Adds the attributes to this instance and returns it. 
	 * <b>Does not returns a novel instance of the dictionnary</b>
	 * @param attributes
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public IGenstarDictionary<A> addAttributes(A... attributes);

	public IGenstarDictionary<A> addAttributes(Collection<A> attributes);

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
