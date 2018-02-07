package core.configuration.jackson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.JsonNodeType;

import core.metamodel.attribute.IAttribute;
import core.metamodel.attribute.IValueSpace;
import core.metamodel.attribute.demographic.DemographicAttribute;
import core.metamodel.attribute.demographic.DemographicAttributeFactory;
import core.metamodel.attribute.demographic.MappedDemographicAttribute;
import core.metamodel.attribute.demographic.map.IAttributeMapper;
import core.metamodel.attribute.emergent.EmergentAttribute;
import core.metamodel.attribute.emergent.EmergentAttributeFactory;
import core.metamodel.attribute.emergent.filter.EntityChildFilterFactory;
import core.metamodel.attribute.emergent.filter.EntityChildFilterFactory.EChildFilter;
import core.metamodel.attribute.emergent.filter.IEntityChildFilter;
import core.metamodel.attribute.emergent.function.EntityAggregatedAttributeFunction;
import core.metamodel.attribute.emergent.function.EntityCountFunction;
import core.metamodel.attribute.emergent.function.EntityValueForAttributeFunction;
import core.metamodel.attribute.emergent.function.IEntityEmergentFunction;
import core.metamodel.attribute.emergent.function.aggregator.IAggregateValueFunction;
import core.metamodel.attribute.geographic.GeographicAttribute;
import core.metamodel.attribute.geographic.GeographicAttributeFactory;
import core.metamodel.attribute.record.RecordAttribute;
import core.metamodel.entity.IEntity;
import core.metamodel.value.IValue;
import core.metamodel.value.binary.BooleanValue;
import core.metamodel.value.categoric.NominalValue;
import core.metamodel.value.categoric.OrderedValue;
import core.metamodel.value.numeric.RangeValue;
import core.util.data.GSEnumDataType;
import core.util.excpetion.GSIllegalRangedData;

public class AttributeDeserializer extends StdDeserializer<IAttribute<? extends IValue>> {
	
	public static Map<String, DemographicAttribute<? extends IValue>> DES_DEMO_ATTRIBUTES = new HashMap<>();
	
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
			case DemographicAttribute.SELF:
				return this.deserializeDA(on);
			case MappedDemographicAttribute.SELF:
				String mapperType = on.get(MappedDemographicAttribute.MAP)
					.get(IAttributeMapper.TYPE).asText();
				switch (mapperType) {
				case IAttributeMapper.REC:
					return this.deserializeRDA(on);
				case IAttributeMapper.AGG:
					return this.deserializeADA(on);
				case IAttributeMapper.UND:
					return this.deserializeUDA(on);
				default:
					throw new IllegalArgumentException("Trying to deserialize unrecognized mapper: "+mapperType);
				}
			case RecordAttribute.SELF:
				return this.deserializeRA(on);
			case GeographicAttribute.SELF:
				return this.deserializeGA(on);
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
		return DES_DEMO_ATTRIBUTES.put(id, attribute);
	}

	/*
	 * Deserialize basic demographic attribute
	 */
	private DemographicAttribute<? extends IValue> deserializeDA(JsonNode node) 
			throws GSIllegalRangedData {
		String id = this.getName(node);
		if(DES_DEMO_ATTRIBUTES.containsKey(id))
			return DES_DEMO_ATTRIBUTES.get(id);
		DemographicAttribute<? extends IValue> attribute = DemographicAttributeFactory.getFactory()
				.createAttribute(id, this.getType(node), this.getValues(node));
		return DES_DEMO_ATTRIBUTES.put(id, attribute);
	}
		
	/*
	 * Deserialize undirected mapped demographic attribute
	 */
	private DemographicAttribute<? extends IValue> deserializeUDA(JsonNode node) throws GSIllegalRangedData {
		String id = this.getName(node);
		if(DES_DEMO_ATTRIBUTES.containsKey(id))
			return DES_DEMO_ATTRIBUTES.get(id);
		DemographicAttribute<? extends IValue> attribute = DemographicAttributeFactory.getFactory()
				.createMappedAttribute(this.getName(node), this.getType(node), 
						this.getReferentAttribute(node), this.getOrderedMapper(node));
		return DES_DEMO_ATTRIBUTES.put(id, attribute);
	}

	/*
	 * Deserialize record demographic attribute
	 */
	private DemographicAttribute<? extends IValue> deserializeRDA(JsonNode node) throws GSIllegalRangedData {
		String id = this.getName(node);
		if(DES_DEMO_ATTRIBUTES.containsKey(id))
			return DES_DEMO_ATTRIBUTES.get(id);
		DemographicAttribute<? extends IValue> attribute = DemographicAttributeFactory.getFactory()
				.createRecordAttribute(this.getName(node), this.getType(node), 
						this.getReferentAttribute(node), this.getOrderedRecord(node));
		return DES_DEMO_ATTRIBUTES.put(id, attribute);
	}

	/*
	 * Deserialize aggregated demographic attribute
	 */
	private DemographicAttribute<? extends IValue> deserializeADA(JsonNode node) 
			throws GSIllegalRangedData{
		String id = this.getName(node);
		if(DES_DEMO_ATTRIBUTES.containsKey(id))
			return DES_DEMO_ATTRIBUTES.get(id);
		
		MappedDemographicAttribute<? extends IValue, ? extends IValue> attribute = null;
		Map<String, Collection<String>> map = this.getOrderedAggregate(node);
		switch (this.getType(node)) {
		case Range:
			attribute = DemographicAttributeFactory.getFactory()
					.createRangeAggregatedAttribute(id, 
							this.deserializeDA(RangeValue.class, node.findValue(MappedDemographicAttribute.REF)),
								map);
			break;
		case Boolean:
			attribute = DemographicAttributeFactory.getFactory()
					.createBooleanAggregatedAttribute(id, 
							this.deserializeDA(BooleanValue.class, node.findValue(MappedDemographicAttribute.REF)),
								map);
			break;
		case Nominal:
			attribute = DemographicAttributeFactory.getFactory()
					.createNominalAggregatedAttribute(id, 
							this.deserializeDA(NominalValue.class, node.findValue(MappedDemographicAttribute.REF)),
								map);
			break;
		case Order:
			attribute = DemographicAttributeFactory.getFactory()
					.createOrderedAggregatedAttribute(id, 
							this.deserializeDA(OrderedValue.class, node.findValue(MappedDemographicAttribute.REF)),
								map.entrySet().stream().collect(Collectors.toMap(
										Entry::getKey, 
										entry -> new ArrayList<>(entry.getValue()),
										(e1, e2) -> e1,
										LinkedHashMap::new)));
			break;
		default:
			throw new IllegalArgumentException("Trying to parse unknown value type: "+this.getType(node));
		}
		return DES_DEMO_ATTRIBUTES.put(id, attribute);
	}
	
	@SuppressWarnings("unchecked")
	private <V extends IValue> DemographicAttribute<V> deserializeDA(Class<V> clazz, JsonNode node) 
			throws GSIllegalRangedData {
		if(!node.asText().isEmpty()) {
			DemographicAttribute<? extends IValue> attribute = DES_DEMO_ATTRIBUTES.get(node.asText());
			if(attribute.getValueSpace().getType().equals(GSEnumDataType.getType(clazz)))
				return (DemographicAttribute<V>) attribute;
			throw new IllegalStateException("Trying to deserialize attribute \""+node.asText()+"\" of type "
					+ attribute.getValueSpace().getType() + " as a " + clazz.getCanonicalName() +" attribute type");
		}
		return DemographicAttributeFactory.getFactory().createAttribute(
				this.getName(node.findValue(DemographicAttribute.SELF)), 
				this.getValues(node.findValue(DemographicAttribute.SELF)), clazz);
	}
	
	/*
	 * Deserialize geographic attribute
	 * FIXME: must follow the whole procedure with value transfert
	 * WARNING: see factory to further enhance capacities
	 */
	private GeographicAttribute<? extends IValue> deserializeGA(JsonNode node){
		return GeographicAttributeFactory.getFactory()
				.createAttribute(this.getName(node), this.getType(node));
	}
	
	/*
	 * Deserialize record attribute
	 */
	private RecordAttribute<? extends IAttribute<? extends IValue>, ? extends IAttribute<? extends IValue>> deserializeRA(JsonNode node) 
			throws GSIllegalRangedData { 
		return DemographicAttributeFactory.getFactory()
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
	 * WARNING: only functional for MappedDemographicAttribute
	 */
	private DemographicAttribute<? extends IValue> getReferentAttribute(JsonNode attributeNode) 
			throws GSIllegalRangedData {
		JsonNode referent = attributeNode.findValue(MappedDemographicAttribute.REF); 
		if(referent.getNodeType().equals(JsonNodeType.STRING))
			return DES_DEMO_ATTRIBUTES.get(referent.asText());
		return this.deserializeDA(attributeNode
				.findValue(MappedDemographicAttribute.REF)
				.findValue(DemographicAttribute.SELF));
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
	@SuppressWarnings("unchecked")
	private EmergentAttribute<? extends IValue, ? extends IEntity<? extends IAttribute<? extends IValue>>, ?> getEmergentAttribute(
			JsonNode node) throws JsonParseException, JsonMappingException, JsonProcessingException, IOException {
		
		String name = this.getName(node);
		JsonNode function = node.findValue(EmergentAttribute.FUNCTION);
		JsonNode filter = function.get(IEntityEmergentFunction.FILTER);
			
		EmergentAttribute<? extends IValue, IEntity<? extends IAttribute<? extends IValue>>, ?> att = null;
		
		ObjectMapper om = new ObjectMapper();
		switch (function.get(IEntityEmergentFunction.TYPE).textValue()) {
		case EntityCountFunction.SELF:
			att = EmergentAttributeFactory.getFactory().getCountAttribute(name, this.getFilter(filter), 
							this.getMatchers(filter.get(IEntityEmergentFunction.MATCHERS)));
			break;
		case EntityAggregatedAttributeFunction.SELF:
			att = EmergentAttributeFactory.getFactory().getAggregatedValueOfAttribute(name,  
					om.readValue(om.writeValueAsBytes(function.get(EntityAggregatedAttributeFunction.AGGREGATOR)), 
							IAggregateValueFunction.class), 
					this.getFilter(filter), this.getMatchers(filter.get(IEntityEmergentFunction.MATCHERS)));
			break;
		case EntityValueForAttributeFunction.SELF:
			String refAtt = function.get(IAttribute.VALUE_SPACE).get(IValueSpace.REF_ATT).asText();
			att = EmergentAttributeFactory.getFactory().getValueOfAttribute(name, 
					DES_DEMO_ATTRIBUTES.get(refAtt), 
					this.getFilter(filter), 
					this.getMatchers(filter.get(IEntityEmergentFunction.MATCHERS)));
			break;
		default:
			throw new IllegalStateException("Emergent function type "
					+function.get(IEntityEmergentFunction.TYPE).textValue()+" is unrecognized");
		}
		
		return att;
	}
	
	/*
	 * Retrieve the filter
	 */
	private IEntityChildFilter getFilter(JsonNode node) throws JsonParseException, JsonMappingException, JsonProcessingException, IOException {
		EChildFilter type = EChildFilter.valueOf(node.get(IEntityChildFilter.TYPE).asText());
		JsonNode comparatorNode = node.get(IEntityChildFilter.COMPARATOR); 
		if(comparatorNode.isNull())
			return type.getFilter();
		ObjectMapper mapper = new ObjectMapper();
		return EntityChildFilterFactory.getFactory().getFilter(type, 
				mapper.readValue(mapper.writeValueAsString(comparatorNode), 
						new TypeReference<Comparator<IEntity<? extends IAttribute<? extends IValue>>>>() { }));
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
	
}
