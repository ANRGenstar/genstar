package core.configuration.dictionary;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import core.configuration.GenstarJsonUtil;
import core.metamodel.IPopulation;
import core.metamodel.attribute.IAttribute;
import core.metamodel.attribute.demographic.DemographicAttribute;
import core.metamodel.attribute.record.RecordAttribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.value.IValue;

/**
 * Encapsulate the whole set of a given attribute type. 
 * For example, it can describe the all {@link DemographicAttribute}
 * used to model a {@link IPopulation} of {@link ADemoEntity} 
 * 
 * @author kevinchapuis
 * @author Samuel Thiriot
 *
 * @param <A>
 */
@JsonTypeName(GenstarJsonUtil.DEMO_DICO)
public class DemographicDictionary<A extends DemographicAttribute<? extends IValue>>
	implements IGenstarDictionary<A> {
	
	private Set<A> attributes;
	private Map<String,A> name2attribute;
	
	private Set<RecordAttribute<A, A>> records;
	
	public DemographicDictionary() {
		this.attributes = new LinkedHashSet<>();
		this.records = new HashSet<>();
		this.name2attribute = new HashMap<>();
	}
	
	/**
	 * Clone constructor
	 * @param d
	 */
	public DemographicDictionary(IGenstarDictionary<A> d) {
		this(d.getAttributes(), d.getRecords());
	}
	
	public DemographicDictionary(Collection<A> attributes) {
		this(attributes, Collections.emptySet());
	}
	
	@JsonCreator
	public DemographicDictionary(
			@JsonProperty(IGenstarDictionary.ATTRIBUTES) Collection<A> attributes,
			@JsonProperty(IGenstarDictionary.RECORDS) Collection<RecordAttribute<A, A>> records) {
		
		if (records == null)
			records = Collections.emptyList();
		
		this.attributes = new LinkedHashSet<>(attributes);
		this.records = new HashSet<>(records);
		this.name2attribute = attributes.stream()
				.collect(Collectors.toMap(
								IAttribute::getAttributeName,
								Function.identity()));
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
		this.name2attribute.putAll(Arrays.asList(attributes).stream()
				.collect(Collectors.toMap(
								IAttribute::getAttributeName,
								Function.identity())));
		return this;
	}
	
	@Override
	public DemographicDictionary<A> addAttributes(Collection<A> attributes) {
		this.attributes.addAll(attributes);
		this.name2attribute.putAll(
				attributes.stream()
							.collect(Collectors.toMap(
								IAttribute::getAttributeName,
								Function.identity())));
		return this;
	}
	
	/*
	@Override
	public void setAttributes(Collection<A> attributes) {
		this.attributes.stream().forEach(att -> name2attribute.remove(att.getAttributeName()));
		this.name2attribute.clear();
		this.attributes.addAll(attributes);
		this.name2attribute = attributes.stream()
						.collect(Collectors.toMap(
								IAttribute::getAttributeName,
								Function.identity()));
	}
	*/

	// ---------------- RECORDS
	
	@SuppressWarnings("unchecked")
	@Override
	public IGenstarDictionary<A> addRecords(RecordAttribute<A, A>... records) {
		this.records.addAll(Arrays.asList(records));
		return this;
	}
	
	@Override
	public Collection<RecordAttribute<A, A>> getRecords() {
		return Collections.unmodifiableSet(records);
	}
	
	/*
	@Override
	public void setRecords(Collection<RecordAttribute<A, A>> records) {
		this.records.clear();
		this.records.addAll(records);
	}
	*/

	// ---------------------------- ACCESSORS ---------------------------- //
	
	/**
	 * Retrieves attributes describe by this dictionary
	 * 
	 * @return
	 */
	@Override
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
		A a = name2attribute.get(string);
		if (a == null)
			throw new NullPointerException("This dictionary contains no reference to the attribute with name "+string);
		return a;
	}
	
	// ------------------- UTILITIES
	
	public boolean containsAttribute(String name) {
		return name2attribute.containsKey(name);
	}
	
	@Override
	public IGenstarDictionary<A> merge(IGenstarDictionary<A> dictionnary) {
		IGenstarDictionary<A> d = new DemographicDictionary<>(this);
		d.addAttributes(dictionnary.getAttributes());
		return d;
	}

	@Override
	public boolean containsValue(String valueStr) {
		for (A a: attributes) {
			if (a.getValueSpace().contains(valueStr))
				return true;
		}
		return false;
	}
	
	@Override
	public int size() {
		return attributes.size();
	}

	
}
