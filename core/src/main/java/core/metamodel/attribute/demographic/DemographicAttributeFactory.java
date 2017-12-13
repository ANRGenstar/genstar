package core.metamodel.attribute.demographic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import core.metamodel.attribute.IValueSpace;
import core.metamodel.attribute.demographic.map.AggregateMapper;
import core.metamodel.attribute.demographic.map.RecordMapper;
import core.metamodel.attribute.demographic.map.UndirectedMapper;
import core.metamodel.attribute.record.RecordAttribute;
import core.metamodel.value.IValue;
import core.metamodel.value.binary.BinarySpace;
import core.metamodel.value.binary.BooleanValue;
import core.metamodel.value.categoric.NominalSpace;
import core.metamodel.value.categoric.NominalValue;
import core.metamodel.value.categoric.OrderedSpace;
import core.metamodel.value.categoric.OrderedValue;
import core.metamodel.value.categoric.template.GSCategoricTemplate;
import core.metamodel.value.numeric.ContinuousSpace;
import core.metamodel.value.numeric.ContinuousValue;
import core.metamodel.value.numeric.IntegerSpace;
import core.metamodel.value.numeric.IntegerValue;
import core.metamodel.value.numeric.RangeSpace;
import core.metamodel.value.numeric.RangeValue;
import core.metamodel.value.numeric.template.GSRangeTemplate;
import core.util.data.GSDataParser;
import core.util.data.GSEnumDataType;
import core.util.excpetion.GSIllegalRangedData;

/**
 * Main factory to build attribute. Anyone that wants to build attribute should
 * refers to one method below: 
 * <p>
 * 1: The 3 methods that return general unidentified value type attribute (i.e. attribute that contains IValue) <br>
 * 2: Each local methods that return specific value type attribute 
 * 
 * @author kevinchapuis
 *
 */
public class DemographicAttributeFactory {
	
	private static DemographicAttributeFactory gaf = new DemographicAttributeFactory();
	
	public static String RECORD_NAME_EXTENSION = "_rec";
	
	private DemographicAttributeFactory(){};
	
	/**
	 * Singleton pattern to setup factory
	 * @return
	 */
	public static DemographicAttributeFactory getFactory() {
		return gaf;
	}
	
	/**
	 * Main method to create attribute with default parameters
	 * 
	 * @param name
	 * @param dataType
	 * @param values
	 * @return
	 * @throws GSIllegalRangedData
	 */
	public DemographicAttribute<? extends IValue> createAttribute(String name, GSEnumDataType dataType) 
			throws GSIllegalRangedData {
		DemographicAttribute<? extends IValue> attribute = null; 
		switch (dataType) {
		case Integer:
			attribute = createIntegerAttribute(name);
			break;
		case Continue:
			attribute = createContinueAttribute(name);
			break;
		case Order:
			attribute = createOrderedAttribute(name, new GSCategoricTemplate());
			break;
		case Nominal:
			attribute = createNominalAttribute(name, new GSCategoricTemplate());
			break;
		case Range:
			throw new IllegalArgumentException("Cannot create range without values to setup template");
		case Boolean:
			attribute = createBooleanAttribute(name);
			break;
		default:
			throw new RuntimeException("Creation attribute failure");
		}
		return attribute;
	}
	
	/**
	 * Main method to create attribute with default parameters
	 * 
	 * @param name
	 * @param dataType
	 * @param values
	 * @return
	 * @throws GSIllegalRangedData
	 */
	public DemographicAttribute<? extends IValue> createAttribute(
			String name, 
			GSEnumDataType dataType,
			List<String> values, List<Object> actualValues) throws GSIllegalRangedData {
		
		if (actualValues == null)
			return createAttribute(name, dataType, values);
		
		assert values.size() == actualValues.size();
		
		DemographicAttribute<? extends IValue> attribute = null;
		try {
			attribute = this.createAttribute(name, dataType);
		} catch (IllegalArgumentException e) {
			attribute = this.createRangeAttribute(name, new GSDataParser().getRangeTemplate(values));
		}
		final IValueSpace<? extends IValue> vs = attribute.getValueSpace(); 
		
		for (int i=0; i<values.size(); i++) {
			IValue val = vs.addValue(values.get(i));
			val.setActualValue(actualValues.get(i));
		}
		System.err.println("["+DemographicAttributeFactory.class.getSimpleName()+"#createAttribute(...)] => "+name+" "+dataType);
		return attribute;
	}
	
	/**
	 * Main method to create an attribute with default parameters
	 * 
	 * @param name
	 * @param dataType
	 * @param values
	 * @return
	 * @throws GSIllegalRangedData
	 */
	public DemographicAttribute<? extends IValue> createAttribute(
			String name, 
			GSEnumDataType dataType,
			List<String> values) throws GSIllegalRangedData {
		
		DemographicAttribute<? extends IValue> attribute = null;
		try {
			attribute = this.createAttribute(name, dataType);
		} catch (IllegalArgumentException e) {
			attribute = this.createRangeAttribute(name, new GSDataParser().getRangeTemplate(values));
		}
		final IValueSpace<? extends IValue> vs = attribute.getValueSpace(); 
		values.stream().forEach(val -> vs.addValue(val));
		System.err.println("["+DemographicAttributeFactory.class.getSimpleName()+"#createAttribute(...)] => "+name+" "+dataType);
		return attribute;
	}
	
	/**
	 * Unsafe cast based creator <p>
	 * WARNING: all as possible, trying not to use this nasty creator !!!
	 * 
	 * @param name
	 * @param type
	 * @return
	 * @throws GSIllegalRangedData 
	 */
	@SuppressWarnings("unchecked")
	public <V extends IValue> DemographicAttribute<V> createAttribute(String name, List<String> values, Class<V> type) 
			throws GSIllegalRangedData {
		DemographicAttribute<V> attribute = null;
		if(GSEnumDataType.Integer.getGenstarType().equals(type))
			attribute = (DemographicAttribute<V>) createIntegerAttribute(name);
		else if(GSEnumDataType.Continue.getGenstarType().equals(type))
			attribute = (DemographicAttribute<V>) createContinueAttribute(name);
		else if(GSEnumDataType.Order.getGenstarType().equals(type))
			attribute = (DemographicAttribute<V>) createOrderedAttribute(name, new GSCategoricTemplate());
		else if(GSEnumDataType.Nominal.getGenstarType().equals(type))
			attribute = (DemographicAttribute<V>) createNominalAttribute(name, new GSCategoricTemplate());
		else if(GSEnumDataType.Boolean.getGenstarType().equals(type))
			attribute = (DemographicAttribute<V>) createBooleanAttribute(name);
		else if(GSEnumDataType.Range.getGenstarType().equals(type))
			attribute = (DemographicAttribute<V>) createRangeAttribute(name, values);
		else
			throw new RuntimeException(type.getCanonicalName()+" has not any "+GSEnumDataType.class.getCanonicalName()
				+" equivalent");
		final IValueSpace<V> vs = attribute.getValueSpace(); 
		values.stream().forEach(val -> vs.addValue(val));
		return attribute;
	}
	
	/**
	 * Main method to create mapped (STS) attribute
	 * 
	 * @see UndirectedMapper
	 * 
	 * @param string
	 * @param type
	 * @param values
	 * @param referent
	 * @param map
	 * @return
	 * @throws GSIllegalRangedData 
	 */
	public <V extends IValue> MappedDemographicAttribute<? extends IValue, V> createMappedAttribute(
			String name, GSEnumDataType dataType, DemographicAttribute<V> referent, 
			Map<Collection<String>, Collection<String>> map) 
					throws GSIllegalRangedData {
		MappedDemographicAttribute<? extends IValue, V> attribute = null;
		switch (dataType) {
		case Integer:
			attribute = createIntegerAttribute(name, referent, map);
			break;
		case Continue:
			attribute = createContinueAttribute(name, referent, map);
			break;
		case Order:
			attribute = createOrderedAttribute(name, referent, map.entrySet().stream()
					.collect(Collectors.toMap(
							entry -> new ArrayList<>(entry.getKey()), 
							Entry::getValue,
							(e1,e2) -> e1,
							LinkedHashMap::new)));
			break;
		case Nominal:
			attribute = createNominalAttribute(name, new GSCategoricTemplate(), referent, map);
			break;
		case Range:
			attribute = createRangeAttribute(name, new GSDataParser().getRangeTemplate(
					map.keySet().stream().flatMap(Collection::stream).collect(Collectors.toList())),
					referent, map);
			break;
		case Boolean:
			attribute = createBooleanAttribute(name, referent, map);
			break;
		default:
			throw new RuntimeException("Cannot instanciate "+dataType+" data type mapped attribute");
		}
		return attribute;
	}
	
	/**
	 * Main method to create record (OTO) attribute
	 * 
	 * @param name
	 * @param dataType
	 * @param referentAttribute
	 * @param record
	 * @return
	 * @throws GSIllegalRangedData
	 */
	public <V extends IValue> MappedDemographicAttribute<? extends IValue, V> createRecordAttribute(
			String name, GSEnumDataType dataType, DemographicAttribute<V> referentAttribute, 
			Map<String, String> record) 
					throws GSIllegalRangedData{
		MappedDemographicAttribute<? extends IValue, V> attribute = null;
		switch (dataType) {
		case Integer: 
			attribute = createIntegerRecordAttribute(name, referentAttribute, record);
			break;
		case Continue:
			attribute = createContinueRecordAttribute(name, referentAttribute, record);
			break;
		case Order:
			attribute = createRecordAttribute(name, dataType, referentAttribute, 
					record.entrySet().stream().collect(Collectors.toMap(
							Entry::getKey, 
							Entry::getValue,
							(e1,e2) -> e1,
							LinkedHashMap::new)));
			break;
		case Nominal:
			attribute = createNominalRecordAttribute(name, referentAttribute, record);
			break;
		case Range:
			attribute = createRangeRecordAttribute(name, referentAttribute, record);
			break;
		case Boolean:
			attribute = createBooleanRecordAttribute(name, referentAttribute, record);
			break;
		default:
			throw new RuntimeException("Cannot instanciate "+dataType+" data type mapped attribute");
		}
		return attribute;
	}
	
	// ------------------------------------------------------------- //
	//                          BUILD METHOD							//
	
	/* ----------------- *
	 * Integer attribute *
	 * ----------------- */
	
	/**
	 * Create integer value attribute
	 * 
	 * @see IntegerSpace
	 * @see IntegerValue
	 * 
	 * @param name
	 * @return
	 */
	public DemographicAttribute<IntegerValue> createIntegerAttribute(String name){
		DemographicAttribute<IntegerValue> attribute = new DemographicAttribute<>(name);
		attribute.setValueSpace(new IntegerSpace(attribute));
		return attribute;
	}
	
	/**
	 * Create integer mapped value attribute
	 * 
	 * @see IntegerSpace
	 * @see IntegerValue
	 * 
	 * @param name
	 * @return
	 */
	public <V extends IValue> MappedDemographicAttribute<IntegerValue, V> createIntegerAttribute(
			String name, DemographicAttribute<V> referentAttribute, 
			Map<Collection<String>, Collection<String>> map){
		UndirectedMapper<IntegerValue, V> mapper = new UndirectedMapper<>(); 
		MappedDemographicAttribute<IntegerValue, V> attribute = new MappedDemographicAttribute<>(name, 
				referentAttribute, mapper);
		attribute.setValueSpace(new IntegerSpace(attribute));
		mapper.setMapper(
				map.keySet().stream().collect(Collectors.toMap(
						key -> key.stream().map(val -> attribute.getValueSpace().addValue(val))
							.collect(Collectors.toSet()), 
						key -> map.get(key).stream().map(val -> referentAttribute.getValueSpace().getValue(val))
							.collect(Collectors.toSet()))));
		mapper.setRelatedAttribute(attribute);
		return attribute;
	}
	
	/**
	 * Create integer record value attribute with given record mapping
	 * 
	 * @param name
	 * @param referentAttribute
	 * @param mapper
	 * @return
	 */
	public <V extends IValue> MappedDemographicAttribute<IntegerValue, V> createIntegerRecordAttribute(String name,
			DemographicAttribute<V> referentAttribute, Map<String, String> record){
		MappedDemographicAttribute<IntegerValue, V> attribute = new MappedDemographicAttribute<>(name, 
				referentAttribute, new RecordMapper<>());
		attribute.getAttributeMapper().setRelatedAttribute(attribute);
		attribute.setValueSpace(new IntegerSpace(attribute));
		
		for(Entry<String, String> entry : record.entrySet()) {
			IntegerValue val1 = attribute.getValueSpace().addValue(entry.getKey());
			V val2 = referentAttribute.getValueSpace().getValue(entry.getValue());
			attribute.addMappedValue(val1, val2);
		}
		
		/*
		record.keySet().stream().forEach(key -> attribute
				.addMappedValue(
						attribute.getValueSpace().addValue(key), 
						referentAttribute.getValueSpace().getValue(record.get(key))
				));
		 */
		return attribute;
	}
	
	/**
	 * Create integer record value attribute (no prior mapping)
	 * 
	 * @param name
	 * @param referentAttribute
	 * @param mapper
	 * @return
	 */
	public <V extends IValue> MappedDemographicAttribute<IntegerValue, V> createIntegerRecordAttribute(String name,
			DemographicAttribute<V> referentAttribute){
		return this.createIntegerRecordAttribute(name, referentAttribute, Collections.emptyMap());
	}
	
	/**
	 * Create integer record attribute
	 * 
	 * @see RecordAttribute
	 * 
	 * @param name
	 * @param referentAttribute
	 * @return
	 * @throws GSIllegalRangedData
	 */
	public RecordAttribute<DemographicAttribute<? extends IValue>, DemographicAttribute<IntegerValue>, IntegerValue> createRecordContingencyAttribute(
			String name, DemographicAttribute<? extends IValue> referentAttribute) throws GSIllegalRangedData{
		return new RecordAttribute<>(name, this.createIntegerAttribute(name+RECORD_NAME_EXTENSION), referentAttribute);
	}
	
	/* ------------------ *
	 * Continue attribute *
	 * ------------------ */
	
	/**
	 * Create continued value attribute
	 * 
	 * @see ContinuousSpace
	 * @see ContinuousValue
	 * 
	 * @param name
	 * @return
	 */
	public DemographicAttribute<ContinuousValue> createContinueAttribute(String name){
		DemographicAttribute<ContinuousValue> ca = new DemographicAttribute<>(name);
		ca.setValueSpace(new ContinuousSpace(ca));
		return ca;
	}
	
	/**
	 * Create continued mapped value attribute
	 * 
	 * @see IntegerSpace
	 * @see IntegerValue
	 * 
	 * @param name : name of the attribute to be created
	 * @param referentAttribute : the attribute to map created attribute with
	 * @param mapper : the map between values - must be a one (aggregated) 
	 * to several (disaggregated) relationship
	 * @return
	 */
	public <V extends IValue> MappedDemographicAttribute<ContinuousValue, V> createContinueAttribute(String name, 
			DemographicAttribute<V> referentAttribute, Map<Collection<String>, Collection<String>> map){
		UndirectedMapper<ContinuousValue, V> mapper = new UndirectedMapper<>();
		MappedDemographicAttribute<ContinuousValue, V> attribute = new MappedDemographicAttribute<>(name, referentAttribute, mapper);
		attribute.setValueSpace(new ContinuousSpace(attribute));
		mapper.setRelatedAttribute(attribute);
		mapper.setMapper(
				map.keySet().stream().collect(Collectors.toMap(
						key -> key.stream().map(val -> attribute.getValueSpace().addValue(val))
							.collect(Collectors.toSet()),
						key -> map.get(key).stream().map(val -> referentAttribute.getValueSpace().getValue(val))
							.collect(Collectors.toSet())))); 
		return attribute;
	}
	
	/**
	 * Create continued aggregated value attribute
	 * 
	 * @param name
	 * @param referentAttribute
	 * @param values
	 * @param mapper
	 * @return
	 */
	public MappedDemographicAttribute<ContinuousValue, ContinuousValue> createContinuedAgregatedAttribute(String name,
			DemographicAttribute<ContinuousValue> referentAttribute, Map<String, Set<String>> map) {
		AggregateMapper<ContinuousValue> mapper = new AggregateMapper<>();
		MappedDemographicAttribute<ContinuousValue, ContinuousValue> attribute = 
				new MappedDemographicAttribute<>(name, referentAttribute, mapper);
		attribute.setValueSpace(new ContinuousSpace(attribute));
		mapper.setRelatedAttribute(attribute);
		mapper.setMapper(
				map.keySet().stream().collect(Collectors.toMap(
						key -> attribute.getValueSpace().addValue(key), 
						key -> map.get(key).stream().map(val -> referentAttribute.getValueSpace().getValue(val))
							.collect(Collectors.toSet()))));
		return attribute;
	}
	
	/**
	 * Create continued record value attribute with given record
	 * 
	 * @param name
	 * @param referentAttribute
	 * @return
	 */
	public <V extends IValue> MappedDemographicAttribute<ContinuousValue, V> createContinueRecordAttribute(
			String name, DemographicAttribute<V> referentAttribute, Map<String, String> record) {
		MappedDemographicAttribute<ContinuousValue, V> attribute = 
				new MappedDemographicAttribute<>(name, referentAttribute, new RecordMapper<>());
		attribute.getAttributeMapper().setRelatedAttribute(attribute);
		attribute.setValueSpace(new ContinuousSpace(attribute));
		record.keySet().stream().forEach(key -> attribute
				.addMappedValue(
						attribute.getValueSpace().addValue(key), 
						referentAttribute.getValueSpace().getValue(record.get(key))
						));
		return attribute;
	}
	
	/**
	 * Create continued record value attribute (no prior mapping)
	 * 
	 * @param name
	 * @param referentAttribute
	 * @return
	 */
	public <V extends IValue> MappedDemographicAttribute<ContinuousValue, V> createContinueRecordAttribute(
			String name, DemographicAttribute<V> referentAttribute) {
		return this.createContinueRecordAttribute(name, referentAttribute, Collections.emptyMap());
	}
	
	/**
	 * Create continuous value record attribute
	 * 
	 * @param name
	 * @param referentAttribute
	 * @return
	 * @throws GSIllegalRangedData
	 */
	public RecordAttribute<DemographicAttribute<? extends IValue>, DemographicAttribute<ContinuousValue>, ContinuousValue> createRecordContinuousAttribute(
			String name, DemographicAttribute<? extends IValue> referentAttribute) throws GSIllegalRangedData{
		return new RecordAttribute<>(name, this.createContinueAttribute(name+RECORD_NAME_EXTENSION), referentAttribute);
	}
	
	/* ----------------- *
	 * Boolean attribute *
	 * ----------------- */
	
	/**
	 * Create boolean attribute
	 * 
	 * @see BinarySpace
	 * @see BinaryValue
	 * 
	 * @param name
	 * @return
	 */
	public DemographicAttribute<BooleanValue> createBooleanAttribute(String name){
		DemographicAttribute<BooleanValue> ba = new DemographicAttribute<>(name);
		ba.setValueSpace(new BinarySpace(ba));
		return ba;
	}
	
	/**
	 * Create boolean mapped value attribute
	 * 
	 * @param name
	 * @param referentAttribute
	 * @param mapper
	 * @return
	 */
	public <V extends IValue> MappedDemographicAttribute<BooleanValue, V> createBooleanAttribute(String name,
			DemographicAttribute<V> referentAttribute, Map<Collection<String>, Collection<String>> map){
		UndirectedMapper<BooleanValue, V> mapper = new UndirectedMapper<>();
		MappedDemographicAttribute<BooleanValue, V> attribute = 
				new MappedDemographicAttribute<>(name, referentAttribute, mapper);
		attribute.setValueSpace(new BinarySpace(attribute));
		mapper.setRelatedAttribute(attribute);
		mapper.setMapper(
				map.keySet().stream().collect(Collectors.toMap(
						key -> key.stream().map(val -> attribute.getValueSpace().addValue(val))
							.collect(Collectors.toSet()), 
						key -> map.get(key).stream().map(val -> referentAttribute.getValueSpace().getValue(val))
							.collect(Collectors.toSet()))));
		return attribute;
	}
	
	/**
	 * Create boolean aggregated value attribute
	 * 
	 * @param name
	 * @param referentAttribute
	 * @param values
	 * @param mapper
	 * @return
	 */
	public MappedDemographicAttribute<BooleanValue, BooleanValue> createBooleanAggregatedAttribute(String name,
			DemographicAttribute<BooleanValue> referentAttribute, Map<String, Collection<String>> map) {
		AggregateMapper<BooleanValue> mapper = new AggregateMapper<>();
		MappedDemographicAttribute<BooleanValue, BooleanValue> attribute = 
				new MappedDemographicAttribute<BooleanValue, BooleanValue>(name, referentAttribute, mapper);
		attribute.setValueSpace(new BinarySpace(attribute));
		mapper.setRelatedAttribute(attribute);
		mapper.setMapper(
				map.keySet().stream().collect(Collectors.toMap(
						key -> attribute.getValueSpace().addValue(key), 
						key -> map.get(key).stream().map(val -> referentAttribute.getValueSpace().getValue(val))
							.collect(Collectors.toSet()))));
		return attribute;
	}

	/**
	 * Create boolean record value attribute with given record
	 * 
	 * @param name
	 * @param referentAttribute
	 * @return
	 */
	public <V extends IValue> MappedDemographicAttribute<BooleanValue, V> createBooleanRecordAttribute(String name,
			DemographicAttribute<V> referentAttribute, Map<String, String> record) {
		MappedDemographicAttribute<BooleanValue, V> attribute = 
				new MappedDemographicAttribute<>(name, referentAttribute, new RecordMapper<>());
		attribute.getAttributeMapper().setRelatedAttribute(attribute);
		attribute.setValueSpace(new BinarySpace(attribute));
		record.keySet().stream().forEach(key -> attribute
				.addMappedValue(
						attribute.getValueSpace().addValue(key),
						referentAttribute.getValueSpace().getValue(record.get(key))
						));
		return attribute;
	}
	
	/**
	 * Create boolean record value attribute (no prior mapping)
	 * 
	 * @param name
	 * @param referentAttribute
	 * @return
	 */
	public <V extends IValue> MappedDemographicAttribute<BooleanValue, V> createBooleanRecordAttribute(String name,
			DemographicAttribute<V> referentAttribute) {
		return this.createBooleanRecordAttribute(name, referentAttribute, Collections.emptyMap());
	}
	
	/* ------------------------- *
	 * Ordered nominal attribute *
	 * ------------------------- */
	
	/**
	 * Create ordered value attribute
	 * 
	 * @see OrderedSpace
	 * @see OrderedValue
	 * 
	 * @param name
	 * @param ct
	 * @return
	 */
	public DemographicAttribute<OrderedValue> createOrderedAttribute(String name, GSCategoricTemplate ct){
		DemographicAttribute<OrderedValue> oa = new DemographicAttribute<>(name);
		oa.setValueSpace(new OrderedSpace(oa, ct));
		return oa;
	}
	
	/**
	 * Create ordered value attribute with given values
	 * 
	 * @see OrderedSpace
	 * @see OrderedValue
	 * 
	 * @param name
	 * @param ct
	 * @return
	 */
	public DemographicAttribute<OrderedValue> createOrderedAttribute(String name, 
			GSCategoricTemplate ct, List<String> values){
		DemographicAttribute<OrderedValue> oa = new DemographicAttribute<>(name);
		oa.setValueSpace(new OrderedSpace(oa, ct));
		values.stream().forEach(value -> oa.getValueSpace().addValue(value));
		return oa;
	}
	
	/**
	 * Create boolean aggregated value attribute
	 * 
	 * @param name
	 * @param referentAttribute
	 * @param values
	 * @param mapper
	 * @return
	 */
	public MappedDemographicAttribute<OrderedValue, OrderedValue> createOrderedAggregatedAttribute(String name,
			GSCategoricTemplate gsCategoricTemplate, DemographicAttribute<OrderedValue> referentAttribute, 
			LinkedHashMap<String, List<String>> map) {
		AggregateMapper<OrderedValue> mapper = new AggregateMapper<>();
		MappedDemographicAttribute<OrderedValue, OrderedValue> attribute = 
				new MappedDemographicAttribute<>(name, referentAttribute, mapper);
		attribute.setValueSpace(new OrderedSpace(attribute, gsCategoricTemplate));
		mapper.setRelatedAttribute(attribute);
		mapper.setMapper(
				map.keySet().stream().collect(Collectors.toMap(
						key -> attribute.getValueSpace().addValue(key), 
						key -> map.get(key).stream().map(val -> referentAttribute.getValueSpace().getValue(val))
							.collect(Collectors.toList()))));
		return attribute;
	}
	
	/**
	 * Create boolean aggregated value attribute
	 * 
	 * @param name
	 * @param referentAttribute
	 * @param values
	 * @param mapper
	 * @return
	 */
	public MappedDemographicAttribute<OrderedValue, OrderedValue> createOrderedAggregatedAttribute(String name,
			DemographicAttribute<OrderedValue> referentAttribute, LinkedHashMap<String, List<String>> mapper) {
		return this.createOrderedAggregatedAttribute(name, new GSCategoricTemplate(), referentAttribute, mapper);
	}
	
	/**
	 * Create ordered mapped value attribute
	 * 
	 * @param name
	 * @param gsCategoricTemplate
	 * @param referentAttribute
	 * @param mapper
	 * @return
	 */
	public <V extends IValue> MappedDemographicAttribute<OrderedValue, V> createOrderedAttribute(String name,
			GSCategoricTemplate gsCategoricTemplate, DemographicAttribute<V> referentAttribute,
			LinkedHashMap<List<String>, Collection<String>> map) {
		UndirectedMapper<OrderedValue, V> mapper = new UndirectedMapper<>();
		MappedDemographicAttribute<OrderedValue, V> attribute = new MappedDemographicAttribute<>(name, referentAttribute, mapper);
		attribute.setValueSpace(new OrderedSpace(attribute, gsCategoricTemplate));
		mapper.setRelatedAttribute(attribute);
		
		LinkedHashMap<Collection<OrderedValue>, Collection<V>> newMap = new LinkedHashMap<>();
		for(Entry<List<String>, Collection<String>> entry : map.entrySet()) {
			List<OrderedValue> keys = entry.getKey().stream()
					.map(val -> attribute.getValueSpace().addValue(val))
					.collect(Collectors.toList());
			List<V> values = entry.getValue().stream()
					.map(val -> referentAttribute.getValueSpace().getValue(val))
					.collect(Collectors.toList());
			newMap.put(keys, values);
		}
		mapper.setMapper(newMap);
		
		/*
		mapper.setMapper( 
				map.keySet().stream().collect(Collectors.toMap(
						key -> key.stream().map(val -> attribute.getValueSpace().addValue(val))
							.collect(Collectors.toList()), 
						key -> map.get(key).stream().map(val -> referentAttribute.getValueSpace().getValue(val))
							.collect(Collectors.toList()),
							(e1, e2) -> e1,
							LinkedHashMap::new)));
							*/
		return attribute;
	}
	
	/**
	 * Create ordered mapped value attribute
	 * 
	 * @param name
	 * @param referentAttribute
	 * @param mapper
	 * @return
	 */
	public <V extends IValue> MappedDemographicAttribute<OrderedValue, V> createOrderedAttribute(String name,
			DemographicAttribute<V> referentAttribute, LinkedHashMap<List<String>, Collection<String>> mapper) {
		return this.createOrderedAttribute(name, new GSCategoricTemplate(), referentAttribute, mapper);
	}
	
	/**
	 * Create ordered record value attribute with given record
	 * 
	 * @param name
	 * @param gsCategoricTemplate
	 * @param referentAttribute
	 * @param mapper
	 * @return
	 */
	public <V extends IValue> MappedDemographicAttribute<OrderedValue, V> createOrderedRecordAttribute(String name,
			GSCategoricTemplate gsCategoricTemplate,  DemographicAttribute<V> referentAttribute, 
			LinkedHashMap<String, String> record){
		MappedDemographicAttribute<OrderedValue, V > attribute = 
				new MappedDemographicAttribute<>(name, referentAttribute, new RecordMapper<>());
		attribute.setValueSpace(new OrderedSpace(attribute, gsCategoricTemplate));
		attribute.getAttributeMapper().setRelatedAttribute(attribute);
		record.keySet().stream().forEach(key -> attribute
				.addMappedValue(
						attribute.getValueSpace().getValue(key), 
						referentAttribute.getValueSpace().getValue(record.get(key))
						));
		return attribute;
	}
	
	/**
	 * Create ordered record value attribute with given record and default template
	 * 
	 * @param name
	 * @param gsCategoricTemplate
	 * @param referentAttribute
	 * @param mapper
	 * @return
	 */
	public <V extends IValue> MappedDemographicAttribute<OrderedValue, V> createOrderedRecordAttribute(String name,
			DemographicAttribute<V> referentAttribute, LinkedHashMap<String, String> record){
		return this.createOrderedRecordAttribute(name, new GSCategoricTemplate(), referentAttribute, record);
	}
	
	/**
	 * Create ordered record value attribute (no prior mapping) and default template
	 * 
	 * @param name
	 * @param gsCategoricTemplate
	 * @param referentAttribute
	 * @param mapper
	 * @return
	 */
	public <V extends IValue> MappedDemographicAttribute<OrderedValue, V> createOrderedRecordAttribute(String name,
			DemographicAttribute<V> referentAttribute){
		return this.createOrderedRecordAttribute(name, referentAttribute, new LinkedHashMap<>());
	}
	
	/* ----------------- *
	 * Nominal attribute *
	 * ----------------- */
	
	/**
	 * Create nominal value attribute
	 * 
	 * @see NominalSpace
	 * @see NominalValue
	 * 
	 * @param name
	 * @param ct
	 * @return
	 */
	public DemographicAttribute<NominalValue> createNominalAttribute(String name, GSCategoricTemplate ct){
		DemographicAttribute<NominalValue> na = new DemographicAttribute<>(name);
		na.setValueSpace(new NominalSpace(na, ct));
		return na;
	}
	
	/**
	 * Create nominal aggregated value attribute
	 * 
	 * @param name
	 * @param gsCategoricTemplate
	 * @param referent
	 * @param mapper
	 * @return
	 */
	public MappedDemographicAttribute<NominalValue, NominalValue> createNominalAggregatedAttribute(String name,
			GSCategoricTemplate gsCategoricTemplate, DemographicAttribute<NominalValue> referentAttribute,
			Map<String, Collection<String>> map) {
		AggregateMapper<NominalValue> mapper = new AggregateMapper<>();
		MappedDemographicAttribute<NominalValue, NominalValue> attribute = 
				new MappedDemographicAttribute<>(name, referentAttribute, mapper);
		attribute.setValueSpace(new NominalSpace(attribute, gsCategoricTemplate));
		attribute.getAttributeMapper().setRelatedAttribute(attribute);
		mapper.setMapper(
				map.keySet().stream().collect(Collectors.toMap(
						key -> attribute.getValueSpace().addValue(key), 
						key -> map.get(key).stream().map(val -> referentAttribute.getValueSpace().getValue(val))
							.collect(Collectors.toList()))));
		return attribute;
	}
	
	/**
	 * Create nominal aggregated value attribute
	 * 
	 * @param name
	 * @param referentAttribute
	 * @param mapper
	 * @return
	 */
	public MappedDemographicAttribute<NominalValue, NominalValue> createNominalAggregatedAttribute(String name,
			DemographicAttribute<NominalValue> referentAttribute, Map<String, Collection<String>> mapper) {
		return this.createNominalAggregatedAttribute(name, new GSCategoricTemplate(), referentAttribute, mapper);
	}
	
	/**
	 * Create nominal mapped value attribute
	 * 
	 * @param name
	 * @param gsCategoricTemplate
	 * @param referent
	 * @param mapper
	 * @return
	 */
	public <V extends IValue> MappedDemographicAttribute<NominalValue, V> createNominalAttribute(String name,
			GSCategoricTemplate gsCategoricTemplate, DemographicAttribute<V> referentAttribute,
			Map<Collection<String>, Collection<String>> map) {
		UndirectedMapper<NominalValue, V> mapper = new UndirectedMapper<>();
		MappedDemographicAttribute<NominalValue, V> attribute = 
				new MappedDemographicAttribute<>(name, referentAttribute, mapper);
		attribute.setValueSpace(new NominalSpace(attribute, gsCategoricTemplate));
		attribute.getAttributeMapper().setRelatedAttribute(attribute);
		mapper.setMapper( 
				map.keySet().stream().collect(Collectors.toMap(
						key -> key.stream().map(val -> attribute.getValueSpace().addValue(val))
							.collect(Collectors.toList()), 
						key -> map.get(key).stream().map(val -> referentAttribute.getValueSpace().getValue(val))
							.collect(Collectors.toList()))));
		return attribute;
	}
	
	/**
	 * Create a nominal record value attribute with given mapping
	 * 
	 * @param string
	 * @param attCouple
	 * @param mapper
	 * @return
	 */
	public <V extends IValue> MappedDemographicAttribute<NominalValue, V> createNominalRecordAttribute(String name, 
			GSCategoricTemplate gsCategoricTemplate, DemographicAttribute<V> referentAttribute,
			Map<String, String> map) {
		MappedDemographicAttribute<NominalValue, V> attribute = 
				new MappedDemographicAttribute<>(name, referentAttribute, new RecordMapper<>());
		attribute.setValueSpace(new NominalSpace(attribute, gsCategoricTemplate));
		attribute.getAttributeMapper().setRelatedAttribute(attribute);
		map.keySet().stream().forEach(key -> attribute
				.addMappedValue(
						attribute.getValueSpace().addValue(key), 
						referentAttribute.getValueSpace().getValue(map.get(key))
						));
		return attribute;
	}
	
	/**
	 * Create a nominal record value attribute with given record and default template
	 * 
	 * @param name
	 * @param referentAttribute
	 * @param mapper
	 * @return
	 */
	public <V extends IValue> MappedDemographicAttribute<NominalValue, V> createNominalRecordAttribute(String name, 
			DemographicAttribute<V> referentAttribute, Map<String, String> record) {
		return this.createNominalRecordAttribute(name, new GSCategoricTemplate(), referentAttribute, record);
	}
	
	/**
	 * Create a nominal record value attribute (no prior mapping) and default template
	 * 
	 * @param name
	 * @param referentAttribute
	 * @param mapper
	 * @return
	 */
	public <V extends IValue> MappedDemographicAttribute<NominalValue, V> createNominalRecordAttribute(String name, 
			DemographicAttribute<V> referentAttribute) {
		return this.createNominalRecordAttribute(name, referentAttribute, Collections.emptyMap());
	}
	
	/* ----------- *
	 * Range value *
	 * ----------- */
	
	/**
	 * Create range value attribute
	 * 
	 * @see RangeSpace
	 * @see RangeValue
	 * 
	 * @param name
	 * @param rt
	 * @return
	 */
	public DemographicAttribute<RangeValue> createRangeAttribute(String name, GSRangeTemplate rt){
		DemographicAttribute<RangeValue> ra = new DemographicAttribute<RangeValue>(name);
		ra.setValueSpace(new RangeSpace(ra, rt));
		return ra;
	}
	
	/**
	 * Create range value attribute with custom bottom and top bounds
	 * 
	 * @see #createRangeAttribute(String, GSRangeTemplate)
	 * 
	 * @param name
	 * @param rt
	 * @param bottomBound
	 * @param topBound
	 * @return
	 */
	public DemographicAttribute<RangeValue> createRangeAttribute(String name, GSRangeTemplate rt,
			Number bottomBound, Number topBound){
		DemographicAttribute<RangeValue> ra = new DemographicAttribute<RangeValue>(name);
		ra.setValueSpace(new RangeSpace(ra, rt, bottomBound, topBound));
		return ra;
	}
	
	/**
	 * Create range value attribute based on a list of range value
	 * 
	 * @param name
	 * @param ranges
	 * @return
	 * @throws GSIllegalRangedData
	 */
	public DemographicAttribute<RangeValue> createRangeAttribute(String name, List<String> ranges) 
			throws GSIllegalRangedData{
		DemographicAttribute<RangeValue> attribute = this.createRangeAttribute(name, 
				new GSDataParser().getRangeTemplate(ranges)); 
		ranges.stream().forEach(value -> attribute.getValueSpace().addValue(value));
		return attribute;
	}
	
	/**
	 * Create range value attribute based on a list of range value
	 * 
	 * @param name
	 * @param ranges
	 * @return
	 * @throws GSIllegalRangedData
	 */
	public DemographicAttribute<RangeValue> createRangeAttribute(String name, List<String> ranges,
			Number bottomBound, Number topBound) 
			throws GSIllegalRangedData{
		DemographicAttribute<RangeValue> attribute = this.createRangeAttribute(name, 
				new GSDataParser().getRangeTemplate(ranges), bottomBound, topBound);
		ranges.stream().forEach(value -> attribute.getValueSpace().addValue(value));
		return attribute;
	}
	
	/**
	 * Create range aggregated value attribute
	 * 
	 * @param name
	 * @param gsCategoricTemplate
	 * @param referent
	 * @param mapper
	 * @return
	 */
	public MappedDemographicAttribute<RangeValue, RangeValue> createRangeAggregatedAttribute(String name,
			GSRangeTemplate rangeTemplate, DemographicAttribute<RangeValue> referentAttribute,
			Map<String, Collection<String>> map) {
		AggregateMapper<RangeValue> mapper = new AggregateMapper<>();
		MappedDemographicAttribute<RangeValue, RangeValue> attribute = 
				new MappedDemographicAttribute<RangeValue, RangeValue>(name, referentAttribute, mapper);
		RangeSpace refRs = (RangeSpace) referentAttribute.getValueSpace();
		attribute.setValueSpace(new RangeSpace(attribute, rangeTemplate, refRs.getMin(), refRs.getMax()));
		attribute.getAttributeMapper().setRelatedAttribute(attribute);
		mapper.setMapper(
				map.keySet().stream().collect(Collectors.toMap(
						key -> attribute.getValueSpace().addValue(key), 
						key -> map.get(key).stream().map(val -> referentAttribute.getValueSpace().getValue(val))
							.collect(Collectors.toList()))));
		return attribute;
	}
	
	/**
	 * Create range aggregated value attribute
	 * 
	 * @param name
	 * @param referent
	 * @param mapper
	 * @return
	 * @throws GSIllegalRangedData 
	 */
	public MappedDemographicAttribute<RangeValue, RangeValue> createRangeAggregatedAttribute(String name,
			DemographicAttribute<RangeValue> referentAttribute,
			Map<String, Collection<String>> map) throws GSIllegalRangedData {
		return this.createRangeAggregatedAttribute(name, 
				new GSDataParser().getRangeTemplate(new ArrayList<>(map.keySet())), 
				referentAttribute, map);
	}
	
	/**
	 * Create mapped range value attribute 
	 * 
	 * @param name
	 * @param rangeTemplate
	 * @param referent
	 * @param mapper
	 * @return
	 */
	public <V extends IValue> MappedDemographicAttribute<RangeValue, V> createRangeAttribute(String name,
			GSRangeTemplate rangeTemplate, DemographicAttribute<V> referentAttribute,
			Map<Collection<String>, Collection<String>> map) {
		UndirectedMapper<RangeValue, V> mapper = new UndirectedMapper<>();
		MappedDemographicAttribute<RangeValue, V> attribute = 
				new MappedDemographicAttribute<>(name, referentAttribute, mapper);
		attribute.setValueSpace(new RangeSpace(attribute, rangeTemplate));
		attribute.getAttributeMapper().setRelatedAttribute(attribute); 
		mapper.setMapper(
				map.keySet().stream().collect(Collectors.toMap(
						key -> key.stream().map(val -> attribute.getValueSpace().addValue(val))
							.collect(Collectors.toList()), 
						key -> map.get(key).stream().map(val -> referentAttribute.getValueSpace().getValue(val))
							.collect(Collectors.toList()))));
		return attribute;
	}

	/**
	 * Create range record value attribute
	 * 
	 * @param name
	 * @param referentAttribute
	 * @param record
	 * @return
	 * @throws GSIllegalRangedData 
	 */
	public <V extends IValue> MappedDemographicAttribute<RangeValue, V> createRangeRecordAttribute(String name,
			DemographicAttribute<V> referentAttribute, Map<String, String> record) throws GSIllegalRangedData {
		MappedDemographicAttribute<RangeValue, V> attribute = 
				new MappedDemographicAttribute<>(name, referentAttribute, new RecordMapper<>());
		attribute.setValueSpace(new RangeSpace(attribute, 
				new GSDataParser().getRangeTemplate(new ArrayList<>(record.keySet()))));
		attribute.getAttributeMapper().setRelatedAttribute(attribute);
		record.keySet().stream().forEach(key -> attribute
				.addMappedValue(
						attribute.getValueSpace().addValue(key), 
						referentAttribute.getValueSpace().getValue(record.get(key))
						));
		return attribute;
	}
	
}
