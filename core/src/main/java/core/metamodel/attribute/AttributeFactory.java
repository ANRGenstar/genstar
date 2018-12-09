package core.metamodel.attribute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import core.metamodel.attribute.emergent.AggregateValueFunction;
import core.metamodel.attribute.emergent.CountValueFunction;
import core.metamodel.attribute.emergent.EntityValueFunction;
import core.metamodel.attribute.emergent.aggregator.IAggregatorValueFunction;
import core.metamodel.attribute.emergent.filter.GSMatchFilter;
import core.metamodel.attribute.emergent.filter.GSMatchSelection;
import core.metamodel.attribute.emergent.filter.GSNoFilter;
import core.metamodel.attribute.emergent.filter.IGSEntitySelector;
import core.metamodel.attribute.mapper.AggregateMapper;
import core.metamodel.attribute.mapper.RecordMapper;
import core.metamodel.attribute.mapper.UndirectedMapper;
import core.metamodel.attribute.mapper.value.EncodedValueMapper;
import core.metamodel.attribute.mapper.value.NumericValueMapper;
import core.metamodel.attribute.record.RecordAttribute;
import core.metamodel.entity.IEntity;
import core.metamodel.entity.comparator.ImplicitEntityComparator;
import core.metamodel.entity.matcher.AttributeVectorMatcher;
import core.metamodel.entity.matcher.MatchType;
import core.metamodel.entity.matcher.TagMatcher;
import core.metamodel.entity.tag.EntityTag;
import core.metamodel.value.IValue;
import core.metamodel.value.IValueSpace;
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
import core.metamodel.value.numeric.RangeValue.RangeBound;
import core.metamodel.value.numeric.template.GSRangeTemplate;
import core.util.GSKeywords;
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
public class AttributeFactory {

	private static AttributeFactory gaf = new AttributeFactory();

	public static Map<String, IAttribute<? extends IValue>> NIUs = new HashMap<>();
	public static Map<String, EmergentAttribute<IntegerValue, 
		Collection<IEntity<? extends IAttribute<? extends IValue>>>, ?>> SIZE_ATT = new HashMap<>();

	private AttributeFactory(){};

	/**
	 * Singleton pattern to setup factory
	 * @return
	 */
	public static AttributeFactory getFactory() {
		return gaf;
	}

	/**
	 * Static way to innitialize <i> not in universe <i/> attribute
	 * <p>
	 * WARNING: should not be used in any population generation, but just to manipulate {@link IValue}
	 *  
	 * @param type
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <V extends IValue> Attribute<V> createNIU(Class<V> type) {
		String name = type.getSimpleName()+GSKeywords.NIU;
		if(NIUs.containsKey(name))
			return (Attribute<V>) NIUs.get(name);
		if(GSEnumDataType.Integer.getGenstarType().equals(type)) {
			Attribute<IntegerValue> att = new Attribute<IntegerValue>(name);
			att.setValueSpace(new IntegerSpace(att));
			return (Attribute<V>) NIUs.put(name, att);
		} else if(GSEnumDataType.Continue.getGenstarType().equals(type)){
			if(NIUs.containsKey(name))
				return (Attribute<V>) NIUs.get(name);
			return (Attribute<V>) NIUs.put(name, new Attribute<ContinuousValue>(name));
		} else if(GSEnumDataType.Order.getGenstarType().equals(type)){
			if(NIUs.containsKey(name))
				return (Attribute<V>) NIUs.get(name);
			return (Attribute<V>) NIUs.put(name, new Attribute<V>(name));
		} else if(GSEnumDataType.Nominal.getGenstarType().equals(type)){
			if(NIUs.containsKey(name))
				return (Attribute<V>) NIUs.get(name);
			return (Attribute<V>) NIUs.put(name, new Attribute<V>(name));
		} else if(GSEnumDataType.Boolean.getGenstarType().equals(type)){
			if(NIUs.containsKey(name))
				return (Attribute<V>) NIUs.get(name);
			return (Attribute<V>) NIUs.put(name, new Attribute<V>(name));
		} else if(GSEnumDataType.Range.getGenstarType().equals(type)){
			if(NIUs.containsKey(name))
				return (Attribute<V>) NIUs.get(name);
			return (Attribute<V>) NIUs.put(name, new Attribute<V>(name));
		} else
			throw new RuntimeException(type.getCanonicalName()+" has not any "+GSEnumDataType.class.getCanonicalName()
					+" equivalent");
	}

	/**
	 * Main method to create attribute with default parameters
	 * 
	 * @param name
	 * @param dataType
	 * @return
	 * @throws GSIllegalRangedData
	 */
	public Attribute<? extends IValue> createAttribute(String name, GSEnumDataType dataType) 
			throws GSIllegalRangedData {
		Attribute<? extends IValue> attribute = null; 
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
	public Attribute<? extends IValue> createAttribute(
			String name, 
			GSEnumDataType dataType,
			List<String> values, List<Object> actualValues) throws GSIllegalRangedData {

		if (actualValues == null)
			return createAttribute(name, dataType, values);

		assert values.size() == actualValues.size();

		Attribute<? extends IValue> attribute = null;
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
		//System.err.println("["+AttributeFactory.class.getSimpleName()+"#createAttribute(...)] => "+name+" "+dataType);
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
	public Attribute<? extends IValue> createAttribute(
			String name, 
			GSEnumDataType dataType,
			List<String> values) throws GSIllegalRangedData {

		Attribute<? extends IValue> attribute = null;
		try {
			attribute = this.createAttribute(name, dataType);
		} catch (IllegalArgumentException e) {
			attribute = this.createRangeAttribute(name, new GSDataParser().getRangeTemplate(values));
		}
		final IValueSpace<? extends IValue> vs = attribute.getValueSpace(); 
		values.stream().forEach(val -> vs.addValue(val));
		//System.err.println("["+AttributeFactory.class.getSimpleName()+"#createAttribute(...)] => "+name+" "+dataType);
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
	public <V extends IValue> Attribute<V> createAttribute(String name, List<String> values, Class<V> type) 
			throws GSIllegalRangedData {
		Attribute<V> attribute = null;
		if(GSEnumDataType.Integer.getGenstarType().equals(type))
			attribute = (Attribute<V>) createIntegerAttribute(name);
		else if(GSEnumDataType.Continue.getGenstarType().equals(type))
			attribute = (Attribute<V>) createContinueAttribute(name);
		else if(GSEnumDataType.Order.getGenstarType().equals(type))
			attribute = (Attribute<V>) createOrderedAttribute(name, new GSCategoricTemplate());
		else if(GSEnumDataType.Nominal.getGenstarType().equals(type))
			attribute = (Attribute<V>) createNominalAttribute(name, new GSCategoricTemplate());
		else if(GSEnumDataType.Boolean.getGenstarType().equals(type))
			attribute = (Attribute<V>) createBooleanAttribute(name);
		else if(GSEnumDataType.Range.getGenstarType().equals(type))
			attribute = (Attribute<V>) createRangeAttribute(name, values);
		else
			throw new RuntimeException(type.getCanonicalName()+" has not any "+GSEnumDataType.class.getCanonicalName()
					+" equivalent");
		final IValueSpace<V> vs = attribute.getValueSpace(); 
		values.stream().forEach(val -> vs.addValue(val));
		return attribute;
	}


	/**
	 * Create an attribute with encoded form (OTO mapping without being a mapped attribute). Values 
	 * can have several encoding form - one main and other string based value using {@link EncodedValueMapper}
	 * \p
	 * WARNING: default records are not provided for integer and continuous value attribute
	 * 
	 * @param name: the name of the attribute
	 * @param dataType: the type of the attribute values
	 * @param values: the values of the attribute
	 * @param record: the mapping between encoded form and corresponding value
	 * @return
	 * @throws GSIllegalRangedData
	 */
	public Attribute<? extends IValue> createAttribute(String name, GSEnumDataType dataType,
			List<String> values, Map<String, String> record) 
					throws GSIllegalRangedData {
		switch (dataType) {
		case Order:
			return createOrderedAttribute(name, new GSCategoricTemplate(), values, record);
		case Nominal:
			return createNominalAttribute(name, new GSCategoricTemplate(), record);
		case Range:
			return createRangeAttribute(name, record);
		case Boolean:
			return createBooleanAttribute(name, record);
		default:
			throw new IllegalArgumentException("Cannot create record attribute for "+dataType+" type of value attribute");
		}
	}

	/**
	 * Main method to create mapped (STS) attribute
	 * 
	 * @see UndirectedMapper
	 * 
	 * @param string
	 * @param type
	 * @param record
	 * @param referent
	 * @param map
	 * @return
	 * @throws GSIllegalRangedData 
	 */
	public <V extends IValue> MappedAttribute<? extends IValue, V> createSTSMappedAttribute(
			String name, GSEnumDataType dataType, Attribute<V> referent, 
			Map<Collection<String>, Collection<String>> map) 
					throws GSIllegalRangedData {
		MappedAttribute<? extends IValue, V> attribute = null;
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
	 * Main method to create mapped (STS) attribute with encoded forms for values using {@link EncodedValueMapper}
	 * 
	 * @param name: the name of the attribute
	 * @param dataType: the type of attribute's value
	 * @param referent: the referent attribute for mapping
	 * @param map: the mapping between values
	 * @param record: the endoded forms of values
	 * @return
	 * @throws GSIllegalRangedData
	 */
	public <V extends IValue> MappedAttribute<? extends IValue, V> createSTSMappedAttribute(
			String name, GSEnumDataType dataType, Attribute<V> referent, 
			Map<Collection<String>, Collection<String>> map, Map<String, String> record) 
					throws GSIllegalRangedData {
		MappedAttribute<? extends IValue, V> att = this.createSTSMappedAttribute(name, dataType, referent, map);
		for(String rec : record.keySet()) {
			att.addRecords(record.get(rec), rec);
		}
		return att;
	}

	/**
	 * Main method to create record attribute: can represent one-to-several value relationship (OTS)
	 * 
	 * @param name
	 * @param dataType
	 * @param referentAttribute
	 * @param record
	 * @return
	 * @throws GSIllegalRangedData
	 */
	public <V extends IValue> MappedAttribute<? extends IValue, V> createOTSMappedAttribute(
			String name, GSEnumDataType dataType, Attribute<V> referentAttribute, 
			Map<String, String> record) 
					throws GSIllegalRangedData{
		MappedAttribute<? extends IValue, V> attribute = null;
		switch (dataType) {
		case Integer: 
			attribute = createIntegerRecordAttribute(name, referentAttribute, record);
			break;
		case Continue:
			attribute = createContinueRecordAttribute(name, referentAttribute, record);
			break;
		case Order:
			attribute = createOrderedRecordAttribute(name, new GSCategoricTemplate(), referentAttribute, 
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
	public RecordAttribute<Attribute<? extends IValue>, Attribute<? extends IValue>> createRecordAttribute(
			String name, GSEnumDataType dataType, Attribute<? extends IValue> referentAttribute) throws GSIllegalRangedData{
		switch (dataType) {
		case Integer:
			return new RecordAttribute<>(name, this.createIntegerAttribute(name+GSKeywords.RECORD_NAME_EXTENSION), referentAttribute);
		case Continue:
			return new RecordAttribute<>(name, this.createContinueAttribute(name+GSKeywords.RECORD_NAME_EXTENSION), referentAttribute);
		default:
			throw new IllegalArgumentException("Cannot create "+dataType+" record attribute - suppose to be "
					+GSEnumDataType.Integer+" or "+GSEnumDataType.Continue);
		}
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
	public Attribute<IntegerValue> createIntegerAttribute(String name){
		Attribute<IntegerValue> attribute = new Attribute<>(name);
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
	public <V extends IValue> MappedAttribute<IntegerValue, V> createIntegerAttribute(
			String name, Attribute<V> referentAttribute, 
			Map<Collection<String>, Collection<String>> map){
		UndirectedMapper<IntegerValue, V> mapper = new UndirectedMapper<>(); 
		MappedAttribute<IntegerValue, V> attribute = new MappedAttribute<>(name, 
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
	public <V extends IValue> MappedAttribute<IntegerValue, V> createIntegerRecordAttribute(String name,
			Attribute<V> referentAttribute, Map<String, String> record){
		MappedAttribute<IntegerValue, V> attribute = new MappedAttribute<>(name, 
				referentAttribute, new RecordMapper<>());
		attribute.getAttributeMapper().setRelatedAttribute(attribute);
		attribute.setValueSpace(new IntegerSpace(attribute));

		for(Entry<String, String> entry : record.entrySet()) {
			IntegerValue val1 = attribute.getValueSpace().addValue(entry.getKey());
			V val2 = referentAttribute.getValueSpace().getValue(entry.getValue());
			attribute.addMappedValue(val1, val2);
		}
		return attribute;
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
	public Attribute<ContinuousValue> createContinueAttribute(String name){
		Attribute<ContinuousValue> ca = new Attribute<>(name);
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
	public <V extends IValue> MappedAttribute<ContinuousValue, V> createContinueAttribute(String name, 
			Attribute<V> referentAttribute, Map<Collection<String>, Collection<String>> map){
		UndirectedMapper<ContinuousValue, V> mapper = new UndirectedMapper<>();
		MappedAttribute<ContinuousValue, V> attribute = new MappedAttribute<>(name, referentAttribute, mapper);
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
	 * @param record
	 * @param mapper
	 * @return
	 */
	public MappedAttribute<ContinuousValue, ContinuousValue> createContinuedAgregatedAttribute(String name,
			Attribute<ContinuousValue> referentAttribute, Map<String, Set<String>> map) {
		AggregateMapper<ContinuousValue> mapper = new AggregateMapper<>();
		MappedAttribute<ContinuousValue, ContinuousValue> attribute = 
				new MappedAttribute<>(name, referentAttribute, mapper);
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
	public <V extends IValue> MappedAttribute<ContinuousValue, V> createContinueRecordAttribute(
			String name, Attribute<V> referentAttribute, Map<String, String> record) {
		MappedAttribute<ContinuousValue, V> attribute = 
				new MappedAttribute<>(name, referentAttribute, new RecordMapper<>());
		attribute.getAttributeMapper().setRelatedAttribute(attribute);
		attribute.setValueSpace(new ContinuousSpace(attribute));
		record.keySet().stream().forEach(key -> attribute
				.addMappedValue(
						attribute.getValueSpace().addValue(key), 
						referentAttribute.getValueSpace().getValue(record.get(key))
						));
		return attribute;
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
	public Attribute<BooleanValue> createBooleanAttribute(String name){
		Attribute<BooleanValue> ba = new Attribute<>(name);
		ba.setValueSpace(new BinarySpace(ba));
		return ba;
	}

	/**
	 * Create boolean attribute with several encoded forms for values using {@link EncodedValueMapper}
	 * @param name: the name of the attribute
	 * @param record: the encoded form of values
	 * @return
	 */
	public Attribute<BooleanValue> createBooleanAttribute(String name,
			Map<String, String> record){
		Attribute<BooleanValue> attB = this.createBooleanAttribute(name);
		for(String rec : record.keySet()) {
			attB.addRecords(record.get(rec), rec);
		}
		return attB;
	}

	// AGG ----------------

	/**
	 * Create boolean mapped value attribute
	 * 
	 * @param name
	 * @param referentAttribute
	 * @param mapper
	 * @return
	 */
	public <V extends IValue> MappedAttribute<BooleanValue, V> createBooleanAttribute(String name,
			Attribute<V> referentAttribute, Map<Collection<String>, Collection<String>> map){
		UndirectedMapper<BooleanValue, V> mapper = new UndirectedMapper<>();
		MappedAttribute<BooleanValue, V> attribute = 
				new MappedAttribute<>(name, referentAttribute, mapper);
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
	 * Create boolean mapped attribute with several encoded forms for values using {@link EncodedValueMapper}
	 *  
	 * @param name: the name of the attribute
	 * @param referentAttribute: the referent attribute with desaggregated values
	 * @param map: the mapping between aggregated and desaggregated data
	 * @param record: the encoded forms of values
	 * @return {@link MappedAttribute}
	 */
	public <V extends IValue> MappedAttribute<BooleanValue, V> createBooleanAttribute(String name,
			Attribute<V> referentAttribute, Map<Collection<String>, Collection<String>> map,
			Map<String, String> record){
		MappedAttribute<BooleanValue, V> attribute = this.createBooleanAttribute(name, referentAttribute, map);
		for(String rec : record.keySet()) {
			attribute.addRecords(record.get(rec), rec);
		}
		return attribute;
	}

	// REC ----------------

	/**
	 * Create boolean record value attribute with given record
	 * 
	 * @param name
	 * @param referentAttribute
	 * @return
	 */
	public <V extends IValue> MappedAttribute<BooleanValue, V> createBooleanRecordAttribute(String name,
			Attribute<V> referentAttribute, Map<String, String> record) {
		MappedAttribute<BooleanValue, V> attribute = 
				new MappedAttribute<>(name, referentAttribute, new RecordMapper<>());
		attribute.getAttributeMapper().setRelatedAttribute(attribute);
		attribute.setValueSpace(new BinarySpace(attribute));
		record.keySet().stream().forEach(key -> attribute
				.addMappedValue(
						attribute.getValueSpace().addValue(key),
						referentAttribute.getValueSpace().getValue(record.get(key))
						));
		return attribute;
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
	public Attribute<OrderedValue> createOrderedAttribute(String name, GSCategoricTemplate ct){
		Attribute<OrderedValue> oa = new Attribute<>(name);
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
	public Attribute<OrderedValue> createOrderedAttribute(String name, 
			GSCategoricTemplate ct, List<String> values){
		Attribute<OrderedValue> oa = new Attribute<>(name);
		oa.setValueSpace(new OrderedSpace(oa, ct));
		values.stream().forEach(value -> oa.getValueSpace().addValue(value));
		return oa;
	}

	/**
	 * Create ordered attribute with several encoded forms (records) using {@link EncodedValueMapper}
	 * 
	 * @param name: the name of the attribute
	 * @param gsCategoricTemplate: the template that enable new value to match a given pattern
	 * @param values: the values
	 * @param record: the encoded forms of value (records)
	 * @return
	 */
	public Attribute<OrderedValue> createOrderedAttribute(String name,
			GSCategoricTemplate gsCategoricTemplate, List<String> values, Map<String, String> record){
		Attribute<OrderedValue> attO = this.createOrderedAttribute(name, gsCategoricTemplate, values);
		for(String rec : record.keySet()) {
			attO.addRecords(record.get(rec), rec);
		}
		return attO;
	}

	// AGG -----------------------

	/**
	 * Create ordered mapped value attribute
	 * 
	 * @param name
	 * @param gsCategoricTemplate
	 * @param referentAttribute
	 * @param mapper
	 * @return
	 */
	public <V extends IValue> MappedAttribute<OrderedValue, V> createOrderedAttribute(String name,
			GSCategoricTemplate gsCategoricTemplate, Attribute<V> referentAttribute,
			LinkedHashMap<List<String>, Collection<String>> map) {
		UndirectedMapper<OrderedValue, V> mapper = new UndirectedMapper<>();
		MappedAttribute<OrderedValue, V> attribute = new MappedAttribute<>(name, referentAttribute, mapper);
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
	public <V extends IValue> MappedAttribute<OrderedValue, V> createOrderedAttribute(String name,
			Attribute<V> referentAttribute, LinkedHashMap<List<String>, Collection<String>> mapper) {
		return this.createOrderedAttribute(name, new GSCategoricTemplate(), referentAttribute, mapper);
	}

	/**
	 * Create ordered aggregated value attribute
	 * 
	 * @param name
	 * @param referentAttribute
	 * @param record
	 * @param mapper
	 * @return
	 */
	public MappedAttribute<OrderedValue, OrderedValue> createOrderedAggregatedAttribute(String name,
			GSCategoricTemplate gsCategoricTemplate, Attribute<OrderedValue> referentAttribute, 
			LinkedHashMap<String, List<String>> map) {
		AggregateMapper<OrderedValue> mapper = new AggregateMapper<>();
		MappedAttribute<OrderedValue, OrderedValue> attribute = 
				new MappedAttribute<>(name, referentAttribute, mapper);
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
	 * Create oredered aggregated value attribute
	 * 
	 * @param name
	 * @param referentAttribute
	 * @param record
	 * @param mapper
	 * @return
	 */
	public MappedAttribute<OrderedValue, OrderedValue> createOrderedAggregatedAttribute(String name,
			Attribute<OrderedValue> referentAttribute, LinkedHashMap<String, List<String>> mapper) {
		return this.createOrderedAggregatedAttribute(name, new GSCategoricTemplate(), referentAttribute, mapper);
	}

	/**
	 * Create oredered aggregated attribute with several encoded forms for values using {@link EncodedValueMapper}
	 * 
	 * @param name: the name of the attribute
	 * @param referentAttribute: the referent attribute with disaggregated values
	 * @param mapper: the mapping between aggregated and disaggregated values
	 * @param record: the various encoded forms of values
	 * @return
	 */
	public MappedAttribute<OrderedValue, OrderedValue> createOrderedAggregatedAttribute(String name,
			Attribute<OrderedValue> referentAttribute, LinkedHashMap<String, List<String>> mapper,
			Map<String, String> record) {
		MappedAttribute<OrderedValue, OrderedValue> attribute = this.createOrderedAggregatedAttribute(
				name, new GSCategoricTemplate(), referentAttribute, mapper);
		for(String rec : record.keySet()) {
			attribute.addRecords(record.get(rec), rec);
		}
		return attribute;
	}

	/**
	 * Create ordered attribute with mapping to numerical data (int, float or range). The map should record
	 * mapping as follow:
	 * <ul>
	 *  <li> {@link IntegerValue} : an unique int in the list at the first index</li>
	 *  <li> {@link ContinuousValue} : an unique double in the list at the first index </li>
	 *  <li> {@link RangeValue} : two int/double value making a range. null value represent bottom or top value range </li>
	 * </ul>
	 * 
	 * @param name
	 * @param referentAttribute
	 * @param mapper
	 * @return
	 */
	public MappedAttribute<IValue, OrderedValue> createOrderedToNumericAttribute(String name,
			Attribute<OrderedValue> referentAttribute, LinkedHashMap<String, List<Number>> map){
		NumericValueMapper<IValue> mapper = new NumericValueMapper<>();
		MappedAttribute<IValue, OrderedValue> attribute = new MappedAttribute<>(name, referentAttribute, mapper);
		for(String ov : map.keySet()) {
			OrderedValue value = referentAttribute.getValueSpace().getValue(ov);
			List<Number> num = map.get(ov);
			if(num.size() == 1)
				mapper.add(value, num.get(0));
			else {
				if(map.get(0) == null) {
					mapper.add(value, num.get(1), RangeBound.LOWER);
				} else if(map.get(1) == null) {
					mapper.add(value, num.get(0), RangeBound.UPPER);
				} else {
					mapper.add(value, num.get(0), num.get(1));
				}
			}

		}
		return attribute;
	}

	// REC ---------------

	/**
	 * Create ordered record value attribute with given record
	 * 
	 * @param name
	 * @param gsCategoricTemplate
	 * @param referentAttribute
	 * @param mapper
	 * @return
	 */
	public <V extends IValue> MappedAttribute<OrderedValue, V> createOrderedRecordAttribute(String name,
			GSCategoricTemplate gsCategoricTemplate,  Attribute<V> referentAttribute, 
			LinkedHashMap<String, String> record){
		MappedAttribute<OrderedValue, V > attribute = 
				new MappedAttribute<>(name, referentAttribute, new RecordMapper<>());
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
	public <V extends IValue> MappedAttribute<OrderedValue, V> createOrderedRecordAttribute(String name,
			Attribute<V> referentAttribute, LinkedHashMap<String, String> record){
		return this.createOrderedRecordAttribute(name, new GSCategoricTemplate(), referentAttribute, record);
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
	public Attribute<NominalValue> createNominalAttribute(String name, GSCategoricTemplate ct){
		Attribute<NominalValue> na = new Attribute<>(name);
		na.setValueSpace(new NominalSpace(na, ct));
		return na;
	}

	/**
	 * Create a nominal attribute with several encoded forms for values using {@link EncodedValueMapper}
	 * 
	 * @param name: the name of the attribute
	 * @param gsCategoricTemplate: the template to match string value to a given pattern
	 * @param record: the encoded forms of the value (records)
	 * @return
	 */
	public Attribute<NominalValue> createNominalAttribute(String name, 
			GSCategoricTemplate gsCategoricTemplate, Map<String, String> record){
		Attribute<NominalValue> attN = this.createNominalAttribute(name, new GSCategoricTemplate());
		record.values().stream().forEach(v -> attN.getValueSpace().addValue(v));
		for(String rec : record.keySet()) {
			attN.addRecords(record.get(rec), rec);
		}
		return attN;
	}

	// AGG -----------------------

	/**
	 * Create nominal mapped value attribute
	 * 
	 * @param name
	 * @param gsCategoricTemplate
	 * @param vs
	 * @param mapper
	 * @return
	 */
	public <V extends IValue> MappedAttribute<NominalValue, V> createNominalAttribute(String name,
			GSCategoricTemplate gsCategoricTemplate, Attribute<V> referentAttribute,
			Map<Collection<String>, Collection<String>> map) {
		UndirectedMapper<NominalValue, V> mapper = new UndirectedMapper<>();
		MappedAttribute<NominalValue, V> attribute = 
				new MappedAttribute<>(name, referentAttribute, mapper);
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
	 * Create nominal aggregated value attribute
	 * 
	 * @param name
	 * @param gsCategoricTemplate
	 * @param vs
	 * @param mapper
	 * @return
	 */
	public MappedAttribute<NominalValue, NominalValue> createNominalAggregatedAttribute(String name,
			GSCategoricTemplate gsCategoricTemplate, Attribute<NominalValue> referentAttribute,
			Map<String, Collection<String>> map) {
		AggregateMapper<NominalValue> mapper = new AggregateMapper<>();
		MappedAttribute<NominalValue, NominalValue> attribute = 
				new MappedAttribute<>(name, referentAttribute, mapper);
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
	public MappedAttribute<NominalValue, NominalValue> createNominalAggregatedAttribute(String name,
			Attribute<NominalValue> referentAttribute, Map<String, Collection<String>> mapper) {
		return this.createNominalAggregatedAttribute(name, new GSCategoricTemplate(), referentAttribute, mapper);
	}

	/**
	 * Create nominal aggregated attribute with several encoded forms for value using {@link EncodedValueMapper}
	 * 
	 * @param name
	 * @param referentAttribute
	 * @param mapper
	 * @param record
	 * @return
	 */
	public MappedAttribute<NominalValue, NominalValue> createNominalAggregatedAttribute(String name,
			Attribute<NominalValue> referentAttribute, Map<String, Collection<String>> mapper,
			Map<String, String> record) {
		MappedAttribute<NominalValue, NominalValue> attribute = this.createNominalAggregatedAttribute(
				name, new GSCategoricTemplate(), referentAttribute, mapper);
		for(String rec : record.keySet()) {
			attribute.addRecords(record.get(rec), rec);
		}
		return attribute;
	}

	// REC ----------------------

	/**
	 * Create a nominal record value attribute with given mapping
	 * 
	 * @param string
	 * @param attCouple
	 * @param mapper
	 * @return
	 */
	public <V extends IValue> MappedAttribute<NominalValue, V> createNominalRecordAttribute(String name, 
			GSCategoricTemplate gsCategoricTemplate, Attribute<V> referentAttribute,
			Map<String, String> map) {
		MappedAttribute<NominalValue, V> attribute = 
				new MappedAttribute<>(name, referentAttribute, new RecordMapper<>());
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
	public <V extends IValue> MappedAttribute<NominalValue, V> createNominalRecordAttribute(String name, 
			Attribute<V> referentAttribute, Map<String, String> record) {
		return this.createNominalRecordAttribute(name, new GSCategoricTemplate(), referentAttribute, record);
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
	public Attribute<RangeValue> createRangeAttribute(String name, GSRangeTemplate rt){
		Attribute<RangeValue> ra = new Attribute<RangeValue>(name);
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
	public Attribute<RangeValue> createRangeAttribute(String name, GSRangeTemplate rt,
			Number bottomBound, Number topBound){
		Attribute<RangeValue> ra = new Attribute<RangeValue>(name);
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
	public Attribute<RangeValue> createRangeAttribute(String name, List<String> ranges) 
			throws GSIllegalRangedData{
		Attribute<RangeValue> attribute = this.createRangeAttribute(name, 
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
	public Attribute<RangeValue> createRangeAttribute(String name, List<String> ranges,
			Number bottomBound, Number topBound) 
					throws GSIllegalRangedData{
		Attribute<RangeValue> attribute = this.createRangeAttribute(name, 
				new GSDataParser().getRangeTemplate(ranges), bottomBound, topBound);
		ranges.stream().forEach(value -> attribute.getValueSpace().addValue(value));
		return attribute;
	}

	/**
	 * Create range attribute with several encoded forms for values using {@link EncodedValueMapper}
	 * 
	 * @param name: the name of the attribute
	 * @param record: the encoded forms of values (records)
	 * @return
	 * @throws GSIllegalRangedData
	 */
	public Attribute<RangeValue> createRangeAttribute(String name,
			Map<String, String> record) throws GSIllegalRangedData {
		Attribute<RangeValue> attR = this.createRangeAttribute(name, new ArrayList<>(record.values()));
		for(String rec : record.keySet()) {
			attR.addRecords(record.get(rec), rec);
		}
		return attR;
	}

	// AGG -----------------------

	/**
	 * Create mapped (STS) range value attribute 
	 * 
	 * @param name
	 * @param rangeTemplate
	 * @param vs
	 * @param mapper
	 * @return
	 */
	public <V extends IValue> MappedAttribute<RangeValue, V> createRangeAttribute(String name,
			GSRangeTemplate rangeTemplate, Attribute<V> referentAttribute,
			Map<Collection<String>, Collection<String>> map) {
		UndirectedMapper<RangeValue, V> mapper = new UndirectedMapper<>();
		MappedAttribute<RangeValue, V> attribute = 
				new MappedAttribute<>(name, referentAttribute, mapper);
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
	 * Create range aggregated (OTS) value attribute
	 * 
	 * @param name
	 * @param gsCategoricTemplate
	 * @param vs
	 * @param mapper
	 * @return
	 */
	public MappedAttribute<RangeValue, RangeValue> createRangeAggregatedAttribute(String name,
			GSRangeTemplate rangeTemplate, Attribute<RangeValue> referentAttribute,
			Map<String, Collection<String>> map) {
		AggregateMapper<RangeValue> mapper = new AggregateMapper<>();
		MappedAttribute<RangeValue, RangeValue> attribute = 
				new MappedAttribute<RangeValue, RangeValue>(name, referentAttribute, mapper);
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
	 * Create range aggregated (OTS) value attribute with several encoded forms for values using {@link EncodedValueMapper}
	 * 
	 * @param name
	 * @param rangeTemplate
	 * @param referentAttribute
	 * @param map
	 * @param record
	 * @return
	 */
	public MappedAttribute<RangeValue, RangeValue> createRangeAggregatedAttribute(String name,
			GSRangeTemplate rangeTemplate, Attribute<RangeValue> referentAttribute,
			Map<String, Collection<String>> map, Map<String, String> record){
		MappedAttribute<RangeValue, RangeValue> attribute = this.createRangeAggregatedAttribute(
				name, rangeTemplate, referentAttribute, map);
		for(String rec : record.keySet()) {
			attribute.addRecords(record.get(rec), rec);
		}
		return attribute;
	}

	/**
	 * Create range aggregated (OTS) value attribute
	 * 
	 * @param name
	 * @param vs
	 * @param mapper
	 * @return
	 * @throws GSIllegalRangedData 
	 */
	public MappedAttribute<RangeValue, RangeValue> createRangeAggregatedAttribute(String name,
			Attribute<RangeValue> referentAttribute,
			Map<String, Collection<String>> map) throws GSIllegalRangedData {
		return this.createRangeAggregatedAttribute(name, 
				new GSDataParser().getRangeTemplate(new ArrayList<>(map.keySet())), 
				referentAttribute, map);
	}

	/**
	 * Create range aggregated (OTS) attribute with several encoded forms for values using {@link EncodedValueMapper}
	 * @param name
	 * @param referentAttribute
	 * @param map
	 * @param record
	 * @return
	 * @throws GSIllegalRangedData
	 */
	public MappedAttribute<RangeValue, RangeValue> createRangeAggregatedAttribute(String name,
			Attribute<RangeValue> referentAttribute,
			Map<String, Collection<String>> map, Map<String, String> record) throws GSIllegalRangedData{
		MappedAttribute<RangeValue, RangeValue> attribute = this.createRangeAggregatedAttribute(
				name, referentAttribute, map);
		for(String rec : record.keySet()) {
			attribute.addRecords(record.get(rec), rec);
		}
		return attribute;
	}

	/* ----------------------- *
	 * 	  EMERGENT ATTRIBUTE   *
	 * ----------------------- */

	// EMERGENT COUNT

	/**
	 * Attribute for size of super-entitys
	 * 
	 * @param name : the name of the attribute
	 * @return
	 */
	public EmergentAttribute<IntegerValue, Collection<IEntity<? extends IAttribute<? extends IValue>>>, ?> 
	createSizeAttribute(String name, Map<String, List<Integer>> mapping) {

		if(!SIZE_ATT.containsKey(name)) {
			EmergentAttribute<IntegerValue, Collection<IEntity<? extends IAttribute<? extends IValue>>>, Object> attribute = 
					new EmergentAttribute<>(name);
			attribute.setValueSpace(new IntegerSpace(attribute));
			attribute.setFunction(new CountValueFunction<IEntity<? extends IAttribute<? extends IValue>>, IntegerValue>(attribute));
			attribute.setTransposer(new GSNoFilter());
			SIZE_ATT.put(name, attribute);
		}
		
		return SIZE_ATT.get(name);

	}

	/**
	 * Same as {@link #createCountAttribute(String)} but with a mapped attribute
	 * 
	 * @param name : the name of the attribute
	 * @param values : the ordered collection of values
	 * @param mapping : the mapping between count (int) and ordered values
	 * @return
	 */
	public EmergentAttribute<OrderedValue, Collection<IEntity<? extends IAttribute<? extends IValue>>>, ?>
	createCountAttribute(String name, List<String> values, Map<Integer, String> mapping) {

		return this.createCountAttribute(name, values, mapping, new GSNoFilter());

	}

	/**
	 * Attribute that will count the number of sub-entities. As for any emergent attribute
	 * it is possible to filter agent before counting; makes it possible to number sub-entities
	 * that match any {@link IValue} predicate
	 * 
	 * @param name : the name of the attribute
	 * @param values : the ordered collection of values
	 * @param mapping : the mapping between count (int) and ordered values
	 * @param matches : the matches to filter the sub-entities to be counted
	 * @return
	 */
	public EmergentAttribute<OrderedValue, Collection<IEntity<? extends IAttribute<? extends IValue>>>, IValue> 
	createCountAttribute(String name, List<String> values, Map<Integer, String> mapping, IValue... matches) {

		return this.createCountAttribute(name, values, mapping, new GSMatchFilter<>(new AttributeVectorMatcher(matches), MatchType.getDefault()));

	}

	/**
	 * Attribute that will count the number of sub-entities. As for any emergent attribute
	 * it is possible to filter agent before counting; makes it possible to number sub-entities
	 * that match any {@link EntityTag} predicate
	 * 
	 * @param name : the name of the attribute
	 * @param values : the ordered collection of values
	 * @param mapping : the mapping between count (int) and ordered values
	 * @param matches : the matches to filter the sub-entities to be counted
	 * @return
	 */
	public EmergentAttribute<OrderedValue, Collection<IEntity<? extends IAttribute<? extends IValue>>>, EntityTag> 
	createCountAttribute(String name, List<String> values, Map<Integer, String> mapping, EntityTag... matches) {

		return this.createCountAttribute(name, values, mapping, new GSMatchFilter<>(new TagMatcher(matches), MatchType.getDefault()));

	}

	/**
	 * Attribute that will count the number of sub-entities. Sub entities can be filtered and selected using any {@link IGSEntitySelector}
	 * that will transpose a super-entity into a collection of sub-entity 
	 * 
	 * @param name
	 * @param referent
	 * @param mapper
	 * @param transposer
	 * @return
	 */
	public <T, U> EmergentAttribute<OrderedValue, Collection<IEntity<? extends IAttribute<? extends IValue>>>, T>
	createCountAttribute(String name, List<String> values, Map<Integer, String> mapping, 
			IGSEntitySelector<Collection<IEntity<? extends IAttribute<? extends IValue>>>, T> transposer) {
		EmergentAttribute<OrderedValue, Collection<IEntity<? extends IAttribute<? extends IValue>>>, T> attribute = 
				new EmergentAttribute<>(name);
		attribute.setValueSpace(new OrderedSpace(attribute, new GSCategoricTemplate()));
		values.forEach(value -> attribute.getValueSpace().addValue(value));
		Map<Integer, OrderedValue> mapper = mapping.keySet().stream().collect(Collectors.toMap(
				Function.identity(), 
				k -> attribute.getValueSpace().getValue(mapping.get(k))
				));
		attribute.setFunction(new CountValueFunction<IEntity<? extends IAttribute<? extends IValue>>, OrderedValue>(attribute, mapper));
		attribute.setTransposer(transposer);
		return attribute;
	}

	// EMERGENT VALUE FOR ATTRIBUTE

	/**
	 * Attribute that will retrieve the value of one particular sub-entity attribute. This is done
	 * following a {@link EntityTag} based filter
	 * 
	 * @param name : the name of the attribute
	 * @param referent : the referent attribute (attribute of sub-entity to retrieve value from)
	 * @param tags : the tags that will identify the individual to pick
	 * @return
	 */
	public <V extends IValue> EmergentAttribute<V, IEntity<? extends IAttribute<? extends IValue>>, EntityTag> 
	createValueOfAttribute(String name, Attribute<V> referent, EntityTag... tags) {

		return this.createValueOfAttribute(name, referent, 
				new GSMatchSelection<>(new TagMatcher(tags), MatchType.getDefault()));

	}


	/**
	 * Attribute that will retrieve the value of one particular sub-entity attribute. This is done
	 * following a {@link IValue} based filter
	 * 
	 * @param name : the name of the attribute
	 * @param referent : the referent attribute (attribute of sub-entity to retrieve value from)
	 * @param comparator : the comparator to sort sub-entities tag matches and pick the first one
	 * @param tags : the tags that will identify the individual to pick
	 * 
	 * @param <V> : the type of value this attribute is made of
	 * 
	 * @return
	 */
	public <V extends IValue> EmergentAttribute<V, IEntity<? extends IAttribute<? extends IValue>>, IValue> 
	createValueOfAttribute(String name, Attribute<V> referent, IValue... matches) {

		return this.createValueOfAttribute(name, referent, 
				new GSMatchSelection<>(new AttributeVectorMatcher(matches), MatchType.getDefault()));

	}

	/**
	 * Attribute that will retrieve the value of one particular sub-entity attribute.
	 * 
	 * @param name : the name of the attribute
	 * @param referent : the referent attribute (attribute of sub-entity to retrieve value from)
	 * @param transposer : filter that will select one sub-entity to be transposed
	 * 
	 * @param <V> : the type of value this attribute is made of
	 * @param <T> : the predicate type to filter sub-entities
	 * 
	 * @return
	 */
	public <V extends IValue, T> EmergentAttribute<V, IEntity<? extends IAttribute<? extends IValue>>, T> 
	createValueOfAttribute(String name, Attribute<V> referent, 
			IGSEntitySelector<IEntity<? extends IAttribute<? extends IValue>>, T> transposer) {

		EmergentAttribute<V, IEntity<? extends IAttribute<? extends IValue>>, T> eAttribute = 
				new EmergentAttribute<>(name);
		
		eAttribute.setValueSpace(referent.getValueSpace());
		eAttribute.setFunction(new EntityValueFunction<>(referent));
		eAttribute.setTransposer(transposer);
		
		return eAttribute;

	}

	/**
	 * Attribute that will get the value of one particular sub-entities. The selection is a two step process:
	 * <ul>
	 *  <li> Select sub entities according to predicate: e.g. sub-entities with a particular attribute value or tagged as {@link EntityTag#Parent}
	 *  <li> If several predicate matches, then sort them according to an {@link ImplicitEntityComparator} and pick the first one
	 * </ul>
	 * 
	 * 
	 * @param name : the name of the attribute
	 * @param referent : the referent attribute (attribute of sub-entity to retrieve value from)
	 * @param mapping : the mapping between super-attribute and sub-attribute
	 * @param transposer : the filter that will transpose super entity to one sub entity {@link IGSEntitySelector}
	 * 
	 * @param <V> : the type of value this attribute is made of
	 * @param <T> : either {@link IValue} or {@link EntityTag}
	 * 
	 * @return
	 */
	public <V extends IValue, T> EmergentAttribute<V, IEntity<? extends IAttribute<? extends IValue>>, T> 
	createValueOfAttribute(String name, Attribute<V> referent, List<String> values, Map<String,String> mapping, 
			IGSEntitySelector<IEntity<? extends IAttribute<? extends IValue>>, T> transposer) {

		if(values.isEmpty() || mapping.isEmpty())
			return this.createValueOfAttribute(name, referent, transposer);
		
		EmergentAttribute<V, IEntity<? extends IAttribute<? extends IValue>>, T> eAttribute = 
				new EmergentAttribute<>(name);

		if(mapping.keySet().stream().anyMatch(key -> !referent.getValueSpace().contains(key)))
			throw new IllegalArgumentException("Trying to setup a irregular mapping: key(s) is (are) missing: "
					+mapping.keySet().stream().filter(key -> !referent.getValueSpace().contains(key))
					.collect(Collectors.joining(GSKeywords.SERIALIZE_ELEMENT_SEPARATOR)));

		eAttribute.setValueSpace(referent.getValueSpace().clone(eAttribute));
		values.forEach(value -> eAttribute.getValueSpace().addValue(value));
		Map<V,V> mapper = mapping.keySet().stream().collect(Collectors.toMap(
				k -> referent.getValueSpace().getValue(k), 
				k -> eAttribute.getValueSpace().getValue(mapping.get(k))
				));
		eAttribute.setFunction(new EntityValueFunction<>(eAttribute, referent, mapper));

		eAttribute.setTransposer(transposer);
		return eAttribute;

	}

	// EMERGENT AGGREGATE

	/**
	 * Attribute that aggregate input values into single output value based on a default aggregator. For example, 
	 * it can be used to sum up the revenue of all individual of a household (and works even if it is integer, continuous or range value)
	 * 
	 * see {@link IAggregatorValueFunction#getDefaultAggregator(Class)}
	 * 
	 * @param name : name of the attribute
	 * @param referent : the input attribute
	 * @param matches : the {@link IValue} matches to setup transposer
	 * 
	 * @param <V> : the value type of the attribute
	 * 
	 * @return
	 */
	public <V extends IValue> EmergentAttribute<V, Collection<IEntity<? extends IAttribute<? extends IValue>>>, IValue> 
	createAggregatedValueOfAttribute(String name, Attribute<V> inputAttribute, IValue... matches) {

		return this.createAggregatedValueOfAttribute(name, inputAttribute, 
				IAggregatorValueFunction.getDefaultAggregator(inputAttribute.getValueSpace().getTypeClass()), 
				new GSMatchFilter<>(new AttributeVectorMatcher(matches), MatchType.getDefault()));

	}

	/**
	 * Attribute that aggregate input values into single output value based on a default aggregator. For example, 
	 * it can be used to sum up the revenue of all individual of a household (and works even if it is integer, continuous or range value)
	 * 
	 * see {@link IAggregatorValueFunction#getDefaultAggregator(Class)}
	 * 
	 * @param name : name of the attribute
	 * @param referent : the input attribute
	 * @param matches : the {@link EntityTag} matches to setup transposer
	 * 
	 * @param <V> : the value type of the attribute
	 * 
	 * @return
	 */
	public <V extends IValue> EmergentAttribute<V, Collection<IEntity<? extends IAttribute<? extends IValue>>>, EntityTag> 
	createAggregatedValueOfAttribute(String name, Attribute<V> inputAttribute, EntityTag... matches) {

		return this.createAggregatedValueOfAttribute(name, inputAttribute, 
				IAggregatorValueFunction.getDefaultAggregator(inputAttribute.getValueSpace().getTypeClass()), 
				new GSMatchFilter<>(new TagMatcher(matches), MatchType.getDefault()));

	}

	/**
	 * Attribute that aggregate input values into single output value of same type based on a custom aggregator and
	 * a custom transposer
	 * 
	 * @param name : name of the attribute
	 * @param inputAttribute : the input attribute
	 * @param aggFunction : the custom function
	 * @param transposer : the transposer
	 * 
	 * @param <V> The input and output value type
	 * @param <T> The return type of the transposer
	 * 
	 * @return
	 */
	public <V extends IValue, T> EmergentAttribute<V, Collection<IEntity<? extends IAttribute<? extends IValue>>>, T> 
	createAggregatedValueOfAttribute(String name, Attribute<V> inputAttribute, IAggregatorValueFunction<V> aggFunction,   
			IGSEntitySelector<Collection<IEntity<? extends IAttribute<? extends IValue>>>, T> transposer) {
		EmergentAttribute<V, Collection<IEntity<? extends IAttribute<? extends IValue>>>, T> eAttribute = 
				new EmergentAttribute<>(name);
		eAttribute.setValueSpace(inputAttribute.getValueSpace().clone(eAttribute));
		eAttribute.setFunction(new AggregateValueFunction<V>(aggFunction, inputAttribute));
		eAttribute.setTransposer(transposer);
		return eAttribute;
	}

	/* EMERGENT TRANSPOSED 


	public <V extends IValue, K extends IValue, U>
	EmergentAttribute<K, V, U> createTransposedValuesAttribute(String name,
			Attribute<V> inputAttribute, IAttributeMapper<K, V> mapper, IValueSpace<K> valueSpace, 
			IGSValueFunction<U, V> function, IGSEntityTransposer<V> filter, IValue... matches){
		EmergentAttribute<K, V, U> eAttribute = new EmergentAttribute<>(name, inputAttribute, mapper);
		eAttribute.setValueSpace(valueSpace);
		eAttribute.setFunction(new EntityTransposedAttributeFunction<>(eAttribute, 
				new MappedTransposedValueFunction<>(transposer), filter, matches));
		return eAttribute;
	}
	 */


	/* --------------------- *
	 * 	  RECORD ATTRIBUTE   *
	 * --------------------- */

	/**
	 * Create range record value attribute
	 * 
	 * @param name
	 * @param referentAttribute
	 * @param record
	 * @return
	 * @throws GSIllegalRangedData 
	 */
	public <V extends IValue> MappedAttribute<RangeValue, V> createRangeRecordAttribute(String name,
			Attribute<V> referentAttribute, Map<String, String> record) throws GSIllegalRangedData {
		MappedAttribute<RangeValue, V> attribute = 
				new MappedAttribute<>(name, referentAttribute, new RecordMapper<>());
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

	// :: UTILES -------------------- //

	/**
	 * Postponed the referent attribution
	 * 
	 * @param referee
	 * @param referent
	 */
	public <V extends IValue> void setReferent(MappedAttribute<? extends IValue, V> referee,
			Attribute<V> referent) {
		referee.setReferentAttribute(referent);
	}

}
