package core.configuration.dictionary;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import core.configuration.GenstarJsonUtil;
import core.metamodel.IPopulation;
import core.metamodel.attribute.IAttribute;
import core.metamodel.attribute.demographic.DemographicAttribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.value.IValue;

/**
 * Encapsulate the whole set of a given attribute type. For example, it can describe the all {@link DemographicAttribute}
 * used to model a {@link IPopulation} of {@link ADemoEntity} 
 * 
 * @author kevinchapuis
 *
 * @param <A>
 */
@JsonTypeName(GenstarJsonUtil.DEMO_DICO)
public class DemographicDictionary<A extends DemographicAttribute<? extends IValue>>
	implements IGenstarDictionary<A> {

	public static final String ATTRIBUTES = "ATTRIBUTES";
	
	private Set<A> attributes;
	
	public DemographicDictionary() {
		this.attributes = new LinkedHashSet<>();
	}
	
	@JsonCreator
	public DemographicDictionary(
			@JsonProperty(DemographicDictionary.ATTRIBUTES) Collection<A> attributes) {
		this();
		this.attributes.addAll(attributes);
	}
	
	// ---------------------------- ADDERS ---------------------------- //
	
	/**
	 * Add attributes to this dictionary
	 * @param attributes
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public DemographicDictionary<A> addAttributes(A... attributes) {
		this.attributes.addAll(Arrays.asList(attributes));
		return this;
	}
	
	@Override
	@JsonProperty(DemographicDictionary.ATTRIBUTES)
	public void setAttributes(Collection<A> attributes) {
		this.attributes.clear();
		this.attributes.addAll(attributes);
	}

	// ---------------------------- ACCESSORS ---------------------------- //
	
	/**
	 * Retrieves attributes describe by this dictionary
	 * 
	 * @return
	 */
	@Override
	@JsonProperty(DemographicDictionary.ATTRIBUTES)
	public Collection<A> getAttributes() {
		return Collections.unmodifiableSet(attributes);
	}
	
	/**
	 * Access to attribute using attribute name define as {@link IAttribute#getAttributeName()}
	 * 
	 * @param string
	 * @return
	 */
	@Override
	public A getAttribute(String string) {
		if(attributes.stream().noneMatch(att -> att.getAttributeName().equals(string)))
			throw new NullPointerException("This dictionary contains no reference to the attribute with name "+string);
		return attributes.stream().filter(att -> att.getAttributeName().equals(string)).findFirst().get();
	}
	
}
