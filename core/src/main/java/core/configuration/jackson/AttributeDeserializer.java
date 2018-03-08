package core.configuration.jackson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.JsonNodeType;

import core.metamodel.attribute.Attribute;
import core.metamodel.attribute.AttributeFactory;
import core.metamodel.attribute.EmergentAttribute;
import core.metamodel.attribute.IAttribute;
import core.metamodel.attribute.MappedAttribute;
import core.metamodel.attribute.emergent.EntityAggregatedAttributeFunction;
import core.metamodel.attribute.emergent.EntityCountFunction;
import core.metamodel.attribute.emergent.EntityValueForAttributeFunction;
import core.metamodel.attribute.emergent.IEntityEmergentFunction;
import core.metamodel.attribute.emergent.aggregator.IAggregatorValueFunction;
import core.metamodel.attribute.emergent.filter.EntityChildFilterFactory;
import core.metamodel.attribute.emergent.filter.EntityChildFilterFactory.EChildFilter;
import core.metamodel.attribute.mapper.IAttributeMapper;
import core.metamodel.attribute.emergent.filter.IEntityChildFilter;
import core.metamodel.attribute.record.RecordAttribute;
import core.metamodel.entity.IEntity;
import core.metamodel.entity.comparator.ImplicitEntityComparator;
import core.metamodel.entity.comparator.function.IComparatorFunction;
import core.metamodel.value.IValue;
import core.metamodel.value.IValueSpace;
import core.metamodel.value.binary.BooleanValue;
import core.metamodel.value.categoric.NominalValue;
import core.metamodel.value.categoric.OrderedValue;
import core.metamodel.value.numeric.RangeValue;
import core.util.data.GSEnumDataType;
import core.util.excpetion.GSIllegalRangedData;

public class AttributeDeserializer extends StdDeserializer<IAttribute<? extends IValue>> {
	
	public static Map<String, Attribute<? extends IValue>> DES_DEMO_ATTRIBUTES = new HashMap<>();
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected AttributeDeserializer() {
		this(null);
	}
	
	protected AttributeDeserializer(Class<?> vc) {
		super(vc);
	}

	@Override
	public IAttribute<? extends IValue> deserialize(JsonParser p, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		ObjectMapper om = (ObjectMapper) p.getCodec();
		JsonNode on = om.readTree(p);
		
		String attributeType = p.getParsingContext().getCurrentName();
		try {
			switch (attributeType) {
			case Attribute.SELF:
				return this.deserializeAttribute(on);
			case MappedAttribute.SELF:
				String mapperType = on.get(MappedAttribute.MAP)
					.get(IAttributeMapper.TYPE).asText();
				switch (mapperType) {
				case IAttributeMapper.REC:
					return this.deserializeRMA(on);
				case IAttributeMapper.AGG:
					return this.deserializeAMA(on);
				case IAttributeMapper.UND:
					return this.deserializeUMA(on);
				default:
					throw new IllegalArgumentException("Trying to deserialize unrecognized mapper: "+mapperType);
				}
			case RecordAttribute.SELF:
				return this.deserializeRA(on);
			case EmergentAttribute.SELF:
				return this.deserializeEA(on);
			default:
				throw new IllegalArgumentException("Trying to parse unknown attribute type: "+attributeType); 
			}
		} catch (GSIllegalRangedData e) {
			e.printStackTrace();
		}
		throw new RuntimeException();
	}
	
	// ------------------ SPECIFIC DESERIALIZER ------------------ //

	/*
	 * Deserialize basic attribute
	 */
	private Attribute<? extends IValue> deserializeAttribute(JsonNode node) 
			throws GSIllegalRangedData {
		String id = this.getName(node);
		if(DES_DEMO_ATTRIBUTES.containsKey(id))
			return DES_DEMO_ATTRIBUTES.get(id);
		Attribute<? extends IValue> attribute = AttributeFactory.getFactory()
				.createAttribute(id, this.getType(node), this.getValues(node));
		DES_DEMO_ATTRIBUTES.put(id, attribute);
		return attribute;
	}
	
	/*
	 * Deserialize emergent attribute
	 */
	private IAttribute<? extends IValue> deserializeEA(JsonNode node) {
		String id = this.getName(node);
		if(DES_DEMO_ATTRIBUTES.containsKey(id))
			return DES_DEMO_ATTRIBUTES.get(id);
		EmergentAttribute<? extends IValue, ? extends IEntity<? extends IAttribute<? extends IValue>>, ?> attribute = null;
		try {
			attribute = this.getEmergentAttribute(node);
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		DES_DEMO_ATTRIBUTES.put(id, attribute);
		return attribute;
	}
		
	/*
	 * Deserialize undirected mapped attribute
	 */
	private Attribute<? extends IValue> deserializeUMA(JsonNode node) throws GSIllegalRangedData {
		String id = this.getName(node);
		if(DES_DEMO_ATTRIBUTES.containsKey(id))
			return DES_DEMO_ATTRIBUTES.get(id);
		Attribute<? extends IValue> attribute = AttributeFactory.getFactory()
				.createMappedAttribute(this.getName(node), this.getType(node), 
						this.getReferentAttribute(node), this.getOrderedMapper(node));
		DES_DEMO_ATTRIBUTES.put(id, attribute);
		return attribute;
	}

	/*
	 * Deserialize record mapped attribute
	 */
	private Attribute<? extends IValue> deserializeRMA(JsonNode node) throws GSIllegalRangedData {
		String id = this.getName(node);
		if(DES_DEMO_ATTRIBUTES.containsKey(id))
			return DES_DEMO_ATTRIBUTES.get(id);
		Attribute<? extends IValue> attribute = AttributeFactory.getFactory()
				.createRecordAttribute(this.getName(node), this.getType(node), 
						this.getReferentAttribute(node), this.getOrderedRecord(node));
		DES_DEMO_ATTRIBUTES.put(id, attribute);
		return attribute;
	}

	/*
	 * Deserialize aggregated attribute
	 */
	private Attribute<? extends IValue> deserializeAMA(JsonNode node) 
			throws GSIllegalRangedData{
		String id = this.getName(node);
		if(DES_DEMO_ATTRIBUTES.containsKey(id))
			return DES_DEMO_ATTRIBUTES.get(id);
		
		MappedAttribute<? extends IValue, ? extends IValue> attribute = null;
		Map<String, Collection<String>> map = this.getOrderedAggregate(node);
		switch (this.getType(node)) {
		case Range:
			attribute = AttributeFactory.getFactory()
					.createRangeAggregatedAttribute(id, 
							this.deserializeAttribute(RangeValue.class, node.findValue(MappedAttribute.REF)),
								map);
			break;
		case Boolean:
			attribute = AttributeFactory.getFactory()
					.createBooleanAggregatedAttribute(id, 
							this.deserializeAttribute(BooleanValue.class, node.findValue(MappedAttribute.REF)),
								map);
			break;
		case Nominal:
			attribute = AttributeFactory.getFactory()
					.createNominalAggregatedAttribute(id, 
							this.deserializeAttribute(NominalValue.class, node.findValue(MappedAttribute.REF)),
								map);
			break;
		case Order:
			attribute = AttributeFactory.getFactory()
					.createOrderedAggregatedAttribute(id, 
							this.deserializeAttribute(OrderedValue.class, node.findValue(MappedAttribute.REF)),
								map.entrySet().stream().collect(Collectors.toMap(
										Entry::getKey, 
										entry -> new ArrayList<>(entry.getValue()),
										(e1, e2) -> e1,
										LinkedHashMap::new)));
			break;
		default:
			throw new IllegalArgumentException("Trying to parse unknown value type: "+this.getType(node));
		}
		DES_DEMO_ATTRIBUTES.put(id, attribute);
		return attribute;
	}
	
	@SuppressWarnings("unchecked")
	private <V extends IValue> Attribute<V> deserializeAttribute(Class<V> clazz, JsonNode node) 
			throws GSIllegalRangedData {
		if(!node.asText().isEmpty()) {
			Attribute<? extends IValue> attribute = DES_DEMO_ATTRIBUTES.get(node.asText());
			if(attribute.getValueSpace().getType().equals(GSEnumDataType.getType(clazz)))
				return (Attribute<V>) attribute;
			throw new IllegalStateException("Trying to deserialize attribute \""+node.asText()+"\" of type "
					+ attribute.getValueSpace().getType() + " as a " + clazz.getCanonicalName() +" attribute type");
		}
		Attribute<V> attribute = AttributeFactory.getFactory().createAttribute(
				this.getName(node.findValue(Attribute.SELF)), 
				this.getValues(node.findValue(Attribute.SELF)), clazz);
		DES_DEMO_ATTRIBUTES.put(attribute.getAttributeName(), attribute);
		return attribute;
	}
	
	/*
	 * Deserialize record attribute
	 */
	private RecordAttribute<? extends IAttribute<? extends IValue>, ? extends IAttribute<? extends IValue>> deserializeRA(JsonNode node) 
			throws GSIllegalRangedData { 
		return AttributeFactory.getFactory()
				.createRecordAttribute(this.getName(node), 
						GSEnumDataType.valueOf(node.findValue(RecordAttribute.PROXY_TYPE).asText()), 
						this.getReferentAttribute(node));
	}
	
	// ------------------ BASIC INNER UTILITIES ------------------ //

	// ATTRIBUTE FIELD
	
	/*
	 * Get the name of an attribute describes as a json node
	 * 
	 */
	private String getName(JsonNode attributeNode) {
		return attributeNode.get(IAttribute.NAME).asText();
	}
	
	/*
	 * Get the type of value within the attribute describes as a json node
	 * 
	 */
	private GSEnumDataType getType(JsonNode attributeNode) {
		return GSEnumDataType.valueOf(attributeNode.findValue(IValueSpace.TYPE).asText());
	}
	
	/*
	 * Get values from an attribute describes as a json node
	 * WARNING: only useful for demographic attribute
	 */
	private List<String> getValues(JsonNode attributeNode){
		return attributeNode.findValue(IAttribute.VALUE_SPACE)
				.findValue(IValueSpace.VALUES)
				.findValues(IValue.VALUE).stream()
				.map(val -> val.asText()).collect(Collectors.toList());
	}
	
	// REFERENT (DEMOGRAHIC) ATTRIBUTE

	/*
	 * Get the referent attribute within the attribute describes as a json node
	 * WARNING: only functional for MappedAttribute
	 */
	private Attribute<? extends IValue> getReferentAttribute(JsonNode attributeNode) 
			throws GSIllegalRangedData {
		JsonNode referent = attributeNode.findValue(MappedAttribute.REF); 
		if(referent.getNodeType().equals(JsonNodeType.STRING))
			return DES_DEMO_ATTRIBUTES.get(referent.asText());
		return this.deserializeAttribute(attributeNode
				.findValue(MappedAttribute.REF)
				.findValue(Attribute.SELF));
	}
	
	// MAPPER
	
	/*
	 * Get the record map for mapped demographic attribute
	 */
	private LinkedHashMap<String, String> getOrderedRecord(JsonNode node) {
		JsonNode mapArray = this.validateMapper(node);
		LinkedHashMap<String, String> record = new LinkedHashMap<>();
		int i = 0;
		while(mapArray.has(i)) {
			String[] keyVal = mapArray.get(i++).asText()
					.split(AttributeMapperSerializer.MATCHER_SYM);
			if(keyVal.length != 2)
				throw new IllegalArgumentException("Not a key / value match but has "+keyVal.length+" match");
			record.put(keyVal[0].trim(), keyVal[1].trim());
		}
		return record;
	}
	
	/*
	 * Get ordered undirected map for mapped demographic attribute
	 */
	private Map<Collection<String>, Collection<String>> getOrderedMapper(JsonNode node){
		JsonNode mapArray = this.validateMapper(node);
		Map<Collection<String>, Collection<String>> mapper = new LinkedHashMap<>();
		int i = 0;
		while(mapArray.has(i)) {
			String[] keyVal = mapArray.get(i++).asText()
					.split(AttributeMapperSerializer.MATCHER_SYM);
			if(keyVal.length != 2)
				throw new IllegalArgumentException("Not a key / value match but has "+keyVal.length+" match");
			mapper.put(Arrays.asList(keyVal[0].split(AttributeMapperSerializer.SPLIT_SYM)).stream()
						.map(key -> key.trim()).collect(Collectors.toList()), 
					Arrays.asList(keyVal[1].split(AttributeMapperSerializer.SPLIT_SYM)).stream()
						.map(val -> val.trim()).collect(Collectors.toList()));
		}
		return mapper;
	}
	
	/*
	 * Get the aggregate map for mapped demographic attribute
	 */
	private Map<String, Collection<String>> getOrderedAggregate(JsonNode node) {
		JsonNode mapArray = this.validateMapper(node);
		LinkedHashMap<String, Collection<String>> mapper = new LinkedHashMap<>();
		int i = 0;
		while(mapArray.has(i)) {
			String[] keyVal = mapArray.get(i++).asText()
					.split(AttributeMapperSerializer.MATCHER_SYM);
			if(keyVal.length != 2)
				throw new IllegalArgumentException("Not a key / value match but has "+keyVal.length+" match");
			mapper.put(keyVal[0].trim(), 
					Arrays.asList(keyVal[1].split(AttributeMapperSerializer.SPLIT_SYM)).stream()
						.map(val -> val.trim()).collect(Collectors.toList()));
		}
		return mapper;
	}
	
	/*
	 * Check if given node is a proper mapper Object
	 */
	private JsonNode validateMapper(JsonNode node) {
		JsonNode mapArray = node.findValue(IAttributeMapper.THE_MAP);
		if(!mapArray.isArray())
			throw new IllegalArgumentException("Trying to unmap the mapper but cannot access array mapping: "
					+ "node type instade is "+mapArray.getNodeType());
		return mapArray;
	}
	
	// EMERGENT
	
	/*
	 * Build emergent attribute 
	 */
	private EmergentAttribute<? extends IValue, ? extends IEntity<? extends IAttribute<? extends IValue>>, ?> getEmergentAttribute(
			JsonNode node) throws JsonParseException, JsonMappingException, JsonProcessingException, IOException {
		
		String name = this.getName(node);
		
		JsonNode function = node.get(EmergentAttribute.FUNCTION);
		JsonNode filter = function.get(IEntityEmergentFunction.FILTER);
			
		EmergentAttribute<? extends IValue, IEntity<? extends IAttribute<? extends IValue>>, ?> att = null;
		String refAtt = node.get(MappedAttribute.REF).asText();
		
		
		IEntityChildFilter f = this.getFilter(filter);
		IValue[] m = this.getMatchers(filter.get(IEntityEmergentFunction.MATCHERS));
		switch (function.get(IEntityEmergentFunction.TYPE).textValue()) {
		case EntityCountFunction.SELF:
			att = AttributeFactory.getFactory().getCountAttribute(name, f, m);
			break;
		case EntityAggregatedAttributeFunction.SELF:
			att = AttributeFactory.getFactory().getAggregatedValueOfAttribute(name, DES_DEMO_ATTRIBUTES.get(refAtt), 
					this.getAggrgatorFunction(function.get(EntityAggregatedAttributeFunction.AGGREGATOR)), f, m);
			break;
		case EntityValueForAttributeFunction.SELF:
			att = AttributeFactory.getFactory().getValueOfAttribute(name, 
					DES_DEMO_ATTRIBUTES.get(refAtt), f, m);
			break;
		default:
			throw new IllegalStateException("Emergent function type "
					+function.get(IEntityEmergentFunction.TYPE).textValue()+" is unrecognized");
		}
		
		return att;
	}


	@SuppressWarnings("unchecked")
	private <V extends IValue> IAggregatorValueFunction<V> getAggrgatorFunction(JsonNode jsonNode) 
			throws JsonParseException, JsonMappingException, IOException {
		return new ObjectMapper().readValue(jsonNode.toString(), IAggregatorValueFunction.class);
	}

	/*
	 * Retrieve the filter
	 */
	private IEntityChildFilter getFilter(JsonNode node) throws JsonParseException, JsonMappingException, JsonProcessingException, IOException {
		EChildFilter type = EChildFilter.valueOf(node.get(IEntityChildFilter.TYPE).asText());
		
		JsonNode comparatorNode = node.get(IEntityChildFilter.COMPARATOR);		
		ImplicitEntityComparator comparator = new ImplicitEntityComparator();
		this.getCompAttributes(comparatorNode.get(ImplicitEntityComparator.ATTRIBUTES_REF))
			.entrySet().stream().forEach(entry -> comparator.setAttribute(entry.getKey(), entry.getValue()));
		this.getCompFunctions(comparatorNode.get(ImplicitEntityComparator.COMP_FUNCTIONS))
			.stream().forEach(function -> comparator.setComparatorFunction(function));
		
		return EntityChildFilterFactory.getFactory().getFilter(type, comparator);
	}

	/*
	 * Retrieve the value in a matcher array
	 */
	private IValue[] getMatchers(JsonNode node) {
		if(!node.isArray())
			throw new IllegalArgumentException("This node is not an array of matchers (node type is "
					+node.getNodeType()+")");
		List<IValue> values = new ArrayList<>();
		int i = 0;
		while(node.has(i)) {
			String val = node.get(i++).asText();
			IValue value = DES_DEMO_ATTRIBUTES.values().stream()
					.filter(att -> att.getValueSpace().contains(val))
					.findFirst().get().getValueSpace().getValue(val);
			values.add(value);
		}
		return values.toArray(new IValue[values.size()]);
	}
	
	/*
	 * Return attribute and relationship (reverse or not) to comparison process
	 */
	private Map<IAttribute<? extends IValue>, Boolean> getCompAttributes(JsonNode arrayAttributes){
		int index = -1;
		Map<IAttribute<? extends IValue>, Boolean> attributes = new HashMap<>();
		while(arrayAttributes.has(++index)) {
			String[] entry = arrayAttributes.get(index).asText()
					.split(EntityComparatorSerializer.REVERSE_SEPARATOR);
			attributes.put(
					DES_DEMO_ATTRIBUTES.get(entry[0]), 
					Boolean.valueOf(entry[1]));
		}
		return attributes;
	}
	
	/*
	 * Return custom comparison function for specific value type
	 */
	private Collection<IComparatorFunction<? extends IValue>> getCompFunctions(JsonNode arrayFunctions) 
			throws JsonParseException, JsonMappingException, IllegalArgumentException, IOException {
		int index = -1;
		Collection<IComparatorFunction<? extends IValue>> functions = new HashSet<>();
		ObjectMapper om = new ObjectMapper();
		while(arrayFunctions.has(++index))
			functions.add(om.readerFor(IComparatorFunction.class)
					.readValue(arrayFunctions.get(index).asText()));
		return functions;
	}
	
}
