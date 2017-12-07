package core.configuration.dictionary;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import core.metamodel.attribute.IAttribute;
import core.metamodel.value.IValue;

@JsonTypeInfo(
	      use = JsonTypeInfo.Id.NAME,
	      include = JsonTypeInfo.As.WRAPPER_OBJECT
	      )
@JsonSubTypes({
    @JsonSubTypes.Type(value = DemographicDictionary.class)
})
public interface IGenstarDictionary<A extends IAttribute<? extends IValue>> {

	public Collection<A> getAttributes();
	
	public A getAttribute(String string);

	public void setAttributes(Collection<A> attributes);

	@SuppressWarnings("unchecked")
	public IGenstarDictionary<A> addAttributes(A... attributes);

	/**
	 * returns the count of attributes
	 * @return
	 */
	public int size();
}
