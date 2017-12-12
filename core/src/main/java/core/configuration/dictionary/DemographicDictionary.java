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
import java.util.stream.Stream;

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
	public static final String RECORDS = "RECORD ATTRIBUTES";
	
	private Set<A> attributes;
	private Set<A> records;
	
	private Map<String,A> name2attribute;
	
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
			@JsonProperty(DemographicDictionary.ATTRIBUTES) Collection<A> attributes,
			@JsonProperty(DemographicDictionary.RECORDS) Collection<A> records) {
		this.attributes = new LinkedHashSet<>(attributes);
		this.records = new HashSet<>(records);
		this.name2attribute = Stream.concat(attributes.stream(), records.stream())
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
	@JsonProperty(DemographicDictionary.ATTRIBUTES)
	public void setAttributes(Collection<A> attributes) {
		this.attributes.stream().forEach(att -> name2attribute.remove(att));
		this.attributes.clear();
		this.attributes.addAll(attributes);
		this.name2attribute = attributes.stream()
						.collect(Collectors.toMap(
								IAttribute::getAttributeName,
								Function.identity()));
	}

	@SuppressWarnings("unchecked")
	@Override
	public IGenstarDictionary<A> addRecords(A... records) {
		this.records.addAll(Arrays.asList(records));
		this.name2attribute.putAll(Arrays.asList(records).stream()
				.collect(Collectors.toMap(
								IAttribute::getAttributeName,
								Function.identity())));
		return this;
	}
	
	@Override
	@JsonProperty(DemographicDictionary.RECORDS)
	public void setRecords(Collection<A> records) {
		this.records.stream().forEach(att -> name2attribute.remove(att));
		this.records.clear();
		this.records.addAll(records);
		this.name2attribute.putAll(records.stream()
				.collect(Collectors.toMap(
								IAttribute::getAttributeName,
								Function.identity())));
	}

	// ---------------------------- ACCESSORS ---------------------------- //
	
	@Override
	@JsonProperty(DemographicDictionary.ATTRIBUTES)
	public Collection<A> getAttributes() {
		return Collections.unmodifiableSet(attributes);
	}
	
	@Override
	public A getAttribute(String string) {
		A a = name2attribute.get(string);
		if (a == null)
			throw new NullPointerException("This dictionary contains no reference to the attribute with name "+string);
		return a;
	}
	
	@Override
	@JsonProperty(DemographicDictionary.RECORDS)
	public Collection<A> getRecords() {
		return Collections.unmodifiableSet(records);
	}

	@Override
	public Collection<A> getAttributesAndRecords() {
		return Stream.concat(attributes.stream(), records.stream())
				.collect(Collectors.toSet());
	}
	

	@Override
	public int size() {
		return attributes.size();
	}
}
