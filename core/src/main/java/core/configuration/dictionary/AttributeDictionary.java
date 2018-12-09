package core.configuration.dictionary;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

import core.configuration.GenstarJsonUtil;
import core.metamodel.IPopulation;
import core.metamodel.attribute.Attribute;
import core.metamodel.attribute.EmergentAttribute;
import core.metamodel.attribute.IAttribute;
import core.metamodel.attribute.record.RecordAttribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.entity.IEntity;
import core.metamodel.value.IValue;
import core.metamodel.value.numeric.IntegerValue;
import core.util.GSKeywords;

/**
 * Encapsulate the whole set of a given attribute type: i.e. It describes all {@link Attribute}
 * used to model a {@link IPopulation} of {@link ADemoEntity} 
 * 
 * @author kevinchapuis
 * @author Samuel Thiriot
 *
 * @param <A>
 */
@JsonTypeName(GenstarJsonUtil.ATT_DICO)
@JsonPropertyOrder({ IGenstarDictionary.ATTRIBUTES, IGenstarDictionary.RECORDS, IGenstarDictionary.SIZE })
public class AttributeDictionary implements IGenstarDictionary<Attribute<? extends IValue>> {
	
	private Set<Attribute<? extends IValue>> attributes;
	private Map<String,Attribute<? extends IValue>> name2attribute;
	
	private Set<RecordAttribute<Attribute<? extends IValue>, Attribute<? extends IValue>>> records;
	
	private EmergentAttribute<IntegerValue, Collection<IEntity<? extends IAttribute<? extends IValue>>>, ?> sizeAttribute;
	
	public AttributeDictionary() {
		this.attributes = new LinkedHashSet<>();
		this.records = new HashSet<>();
		this.name2attribute = new HashMap<>();
	}
	
	/**
	 * Clone constructor
	 * @param d
	 */
	public AttributeDictionary(IGenstarDictionary<Attribute<? extends IValue>> d) {
		this(d.getAttributes(), d.getRecords(), d.getSizeAttribute());
	}
	
	public AttributeDictionary(Collection<Attribute<? extends IValue>> attributes) {
		this(attributes, Collections.emptySet(), null);
	}
	
	@JsonCreator
	public AttributeDictionary(
			@JsonProperty(IGenstarDictionary.ATTRIBUTES) Collection<Attribute<? extends IValue>> attributes,
			@JsonProperty(IGenstarDictionary.RECORDS) Collection<RecordAttribute<Attribute<? extends IValue>, Attribute<? extends IValue>>> records,
			@JsonProperty(IGenstarDictionary.SIZE) EmergentAttribute<IntegerValue, Collection<IEntity<? extends IAttribute<? extends IValue>>>, ?> sizeAttribute) {
		
		if (records == null)
			records = Collections.emptyList();
		
		this.attributes = new LinkedHashSet<>(attributes);
		this.records = new HashSet<>(records);
		this.sizeAttribute = sizeAttribute;
		this.name2attribute = attributes.stream()
				.collect(Collectors.toMap(
								IAttribute::getAttributeName,
								Function.identity()));
		this.name2attribute.put(GSKeywords.ENTITY_SIZE_ATTRIBUTE, sizeAttribute);
	}
	
	// ---------------------------- ADDERS ---------------------------- //
	
	/**
	 * Add attributes to this dictionary
	 * @param attributes
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public AttributeDictionary addAttributes(Attribute<? extends IValue>... attributes) {
		this.attributes.addAll(Arrays.asList(attributes));
		this.name2attribute.putAll(Arrays.asList(attributes).stream()
				.collect(Collectors.toMap(
								IAttribute::getAttributeName,
								Function.identity())));
		return this;
	}
	
	@Override
	public AttributeDictionary addAttributes(Collection<Attribute<? extends IValue>> attributes) {
		this.attributes.addAll(attributes);
		this.name2attribute.putAll(
				attributes.stream()
							.collect(Collectors.toMap(
								IAttribute::getAttributeName,
								Function.identity())));
		return this;
	}

	// ---------------- RECORDS
	
	@SuppressWarnings("unchecked")
	@Override
	public IGenstarDictionary<Attribute<? extends IValue>> addRecords(
			RecordAttribute<Attribute<? extends IValue>, Attribute<? extends IValue>>... records) {
		this.records.addAll(Arrays.asList(records));
		return this;
	}
	
	@Override
	public Collection<RecordAttribute<Attribute<? extends IValue>, Attribute<? extends IValue>>> getRecords() {
		return Collections.unmodifiableSet(records);
	}
	
	// ----------------- SIZE
	
	public void setSizeAttribute(EmergentAttribute<IntegerValue, Collection<IEntity<? extends IAttribute<? extends IValue>>>, ?> sizeAttribute) {
		this.sizeAttribute = sizeAttribute;
		this.name2attribute.put(GSKeywords.ENTITY_SIZE_ATTRIBUTE, sizeAttribute);
	}
	
	@Override
	public EmergentAttribute<IntegerValue, Collection<IEntity<? extends IAttribute<? extends IValue>>>, ?> getSizeAttribute() {
		return this.sizeAttribute;
	}

	// ---------------------------- ACCESSORS ---------------------------- //
	
	@Override
	public Collection<Attribute<? extends IValue>> getAttributes() {
		return Collections.unmodifiableSet(attributes);
	}
	
	@Override
	public Attribute<? extends IValue> getAttribute(String string) {
		Attribute<? extends IValue> a = name2attribute.get(string);
		if (a == null)
			throw new NullPointerException("This dictionary contains no reference to the attribute with name "+string);
		return a;
	}
	
	@Override
	public IValue getValue(String value) {
		Optional<Attribute<? extends IValue>> attribute = attributes.stream()
				.filter(a -> a.getValueSpace().contains(value))
				.findFirst();
		if(attribute.isPresent())
			return attribute.get().getValueSpace().getValue(value);
		throw new NullPointerException("This dictionary contains no reference to the value "+value);
	}
	
	// ------------------- UTILITIES
	
	public boolean containsAttribute(String name) {
		return name2attribute.containsKey(name);
	}
	
	public boolean containsRecord(String name) {
		return records.stream().anyMatch(rec -> rec.getAttributeName().equals(name));
	}

	@Override
	public boolean containsValue(String valueStr) {
		for (Attribute<? extends IValue> a: attributes) {
			if (a.getValueSpace().contains(valueStr))
				return true;
		}
		return false;
	}
	
	@Override
	public Collection<IAttribute<? extends IValue>> getAttributeAndRecord() {
		return Stream.concat(attributes.stream(), records.stream())
				.collect(Collectors.toCollection(HashSet::new));
	}
	
	@Override
	public IGenstarDictionary<Attribute<? extends IValue>> merge(IGenstarDictionary<Attribute<? extends IValue>> dictionnary) {
		IGenstarDictionary<Attribute<? extends IValue>> d = new AttributeDictionary(this);
		d.addAttributes(dictionnary.getAttributes());
		return d;
	}
	
	@Override
	public int size() {
		return attributes.size();
	}
	
}
