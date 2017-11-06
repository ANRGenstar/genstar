package core.metamodel.attribute.demographic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import core.metamodel.attribute.IValueSpace;
import core.metamodel.value.IValue;
import core.metamodel.value.binary.BinarySpace;
import core.metamodel.value.binary.BooleanValue;
import core.metamodel.value.categoric.NominalSpace;
import core.metamodel.value.categoric.NominalValue;
import core.metamodel.value.categoric.OrderedSpace;
import core.metamodel.value.categoric.OrderedValue;
import core.metamodel.value.categoric.template.GSCategoricTemplate;
import core.metamodel.value.numeric.ContinuedSpace;
import core.metamodel.value.numeric.ContinuedValue;
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
	public DemographicAttribute<? extends IValue> createAttribute(String name, GSEnumDataType dataType,
			List<String> values) throws GSIllegalRangedData {
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
			attribute = createRangeAttribute(name, new GSDataParser().getRangeTemplate(values));
			break;
		case Boolean:
			attribute = createBooleanAttribute(name);
			break;
		default:
			throw new RuntimeException("Creation attribute failure");
		}	
		final IValueSpace<? extends IValue> vs = attribute.getValueSpace(); 
		values.stream().forEach(val -> vs.addValue(val));
		return attribute;
	}
	
	/**
	 * TODO javadoc  
	 * 
	 * @see STSDemographicAttribute
	 * 
	 * @param string
	 * @param type
	 * @param values
	 * @param referent
	 * @param mapper
	 * @return
	 * @throws GSIllegalRangedData 
	 */
	public <V extends IValue> STSDemographicAttribute<? extends IValue, V> createMappedAttribute(String name, GSEnumDataType dataType,
			DemographicAttribute<V> referent, Map<Set<String>, Set<String>> mapper) 
					throws GSIllegalRangedData {
		STSDemographicAttribute<? extends IValue, V> attribute = null;
		switch (dataType) {
		case Integer:
			attribute = createIntegerAttribute(name, referent, mapper);
			break;
		case Continue:
			attribute = createContinueAttribute(name, referent, mapper);
			break;
		case Order:
			attribute = createOrderedAttribute(name, new GSCategoricTemplate(), referent, mapper);
			break;
		case Nominal:
			attribute = createNominalAttribute(name, new GSCategoricTemplate(), referent, mapper);
			break;
		case Range:
			attribute = createRangeAttribute(name, new GSDataParser().getRangeTemplate(
					mapper.keySet().stream().flatMap(Set::stream).collect(Collectors.toList())),
					referent, mapper);
			break;
		case Boolean:
			attribute = createBooleanAttribute(name, referent, mapper);
			break;
		default:
			throw new RuntimeException("Cannot instanciate "+dataType+" data type mapped attribute");
		}
		return attribute;
	}

	
	/**
	 * In case we have better information about an attribute after its definition - for instance 
	 * because the definition was imprecise but is better defined when reading the data - then 
	 * we can create a novel attribute based on the past one by changing or or several of its properties
	 * 
	 * FIXME: fix based on new {@link DemographicAttribute} creation rules
	 * 
	 * @param orignalAtt
	 * @param datatype
	 * @return
	 * @throws GSIllegalRangedData
	 */
	public DemographicAttribute<? extends IValue> createRefinedAttribute(DemographicAttribute<? extends IValue> orignalAtt, 
			GSEnumDataType datatype) throws GSIllegalRangedData {

		//Map<Set<String>, Set<String>> mapper = null;
		
		DemographicAttribute<? extends IValue> novel = createAttribute(
				orignalAtt.getAttributeName(),
				datatype, 
				orignalAtt.getValueSpace().stream().map(a->a.getStringValue()).collect(Collectors.toList()) /*,
				orignalAtt.getReferentAttribute(), 
				mapper */
				);
		return novel;
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
		DemographicAttribute<IntegerValue> attribute = null;
		attribute = new DemographicAttribute<>(name, new IntegerSpace(attribute)); 
		return attribute;
	}
	
	/**
	 * Create integer aggregated value attribute
	 * 
	 * @param name
	 * @param referentAttribute
	 * @param values
	 * @param mapper
	 * @return
	 */
	public OTSDemographicAttribute<IntegerValue> createIntegerAgregatedAttribute(String name,
			DemographicAttribute<IntegerValue> referentAttribute, Map<String, Set<String>> mapper) {
		OTSDemographicAttribute<IntegerValue> attribute = null;
		IntegerSpace is = new IntegerSpace(attribute);
		attribute = new OTSDemographicAttribute<IntegerValue>(name, is, referentAttribute, 
				mapper.keySet().stream().collect(Collectors.toMap(
						key -> is.addValue(key), 
						key -> mapper.get(key).stream().map(val -> referentAttribute.getValueSpace().getValue(val))
							.collect(Collectors.toSet()))));
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
	public <V extends IValue> STSDemographicAttribute<IntegerValue, V> createIntegerAttribute(String name, 
			DemographicAttribute<V> referentAttribute, 
			Map<Set<String>, Set<String>> mapper){
		STSDemographicAttribute<IntegerValue, V> attribute = null;
		IntegerSpace is = new IntegerSpace(attribute);
		attribute = new STSDemographicAttribute<IntegerValue, V>(name, is, referentAttribute, 
				mapper.keySet().stream().collect(Collectors.toMap(
						key -> key.stream().map(val -> is.addValue(val))
							.collect(Collectors.toSet()), 
						key -> mapper.get(key).stream().map(val -> referentAttribute.getValueSpace().getValue(val))
							.collect(Collectors.toSet())))); 
		return attribute;
	}
	
	/**
	 * Create integer record value attribute
	 * 
	 * @param name
	 * @param referentAttribute
	 * @param mapper
	 * @return
	 */
	public <V extends IValue> OTODemographicAttribute<IntegerValue, V> createIntegerRecordAttribute(String name,
			DemographicAttribute<V> referentAttribute){
		OTODemographicAttribute<IntegerValue, V> attribute = null;
		IntegerSpace is = new IntegerSpace(attribute);
		attribute = new OTODemographicAttribute<IntegerValue, V>(name, is, referentAttribute, 
				Collections.emptyMap());
		return attribute;
	}

	
	/* ------------------ *
	 * Continue attribute *
	 * ------------------ */
	
	/**
	 * Create continued value attribute
	 * 
	 * @see ContinuedSpace
	 * @see ContinuedValue
	 * 
	 * @param name
	 * @return
	 */
	public DemographicAttribute<ContinuedValue> createContinueAttribute(String name){
		DemographicAttribute<ContinuedValue> ca = null; 
		ca = new DemographicAttribute<>(name, new ContinuedSpace(ca));
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
	public <V extends IValue> STSDemographicAttribute<ContinuedValue, V> createContinueAttribute(String name, 
			DemographicAttribute<V> referentAttribute, Map<Set<String>, Set<String>> mapper){
		STSDemographicAttribute<ContinuedValue, V> attribute = null;
		ContinuedSpace is = new ContinuedSpace(attribute);
		attribute = new STSDemographicAttribute<ContinuedValue, V>(name, is, referentAttribute, 
				mapper.keySet().stream().collect(Collectors.toMap(
						key -> key.stream().map(val -> is.addValue(val))
							.collect(Collectors.toSet()),
						key -> mapper.get(key).stream().map(val -> referentAttribute.getValueSpace().getValue(val))
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
	public OTSDemographicAttribute<ContinuedValue> createContinuedAgregatedAttribute(String name,
			DemographicAttribute<ContinuedValue> referentAttribute, Map<String, Set<String>> mapper) {
		OTSDemographicAttribute<ContinuedValue> attribute = null;
		ContinuedSpace is = new ContinuedSpace(attribute);
		attribute = new OTSDemographicAttribute<ContinuedValue>(name, is, referentAttribute, 
				mapper.keySet().stream().collect(Collectors.toMap(
						key -> is.addValue(key), 
						key -> mapper.get(key).stream().map(val -> referentAttribute.getValueSpace().getValue(val))
							.collect(Collectors.toSet()))));
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
	public DemographicAttribute<BooleanValue> createBooleanAttribute(String name){
		DemographicAttribute<BooleanValue> ba = null;
		ba = new DemographicAttribute<>(name, new BinarySpace(ba));
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
	public <V extends IValue> STSDemographicAttribute<BooleanValue, V> createBooleanAttribute(String name,
			DemographicAttribute<V> referentAttribute, Map<Set<String>, Set<String>> mapper){
		STSDemographicAttribute<BooleanValue, V> attribute = null;
		BinarySpace bs = new BinarySpace(attribute);
		attribute = new STSDemographicAttribute<BooleanValue, V>(name, bs, referentAttribute, 
				mapper.keySet().stream().collect(Collectors.toMap(
						key -> key.stream().map(val -> bs.addValue(val))
							.collect(Collectors.toSet()), 
						key -> mapper.get(key).stream().map(val -> referentAttribute.getValueSpace().getValue(val))
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
	public OTSDemographicAttribute<BooleanValue> createBooleanAgregatedAttribute(String name,
			DemographicAttribute<BooleanValue> referentAttribute, Map<String, Set<String>> mapper) {
		OTSDemographicAttribute<BooleanValue> attribute = null;
		BinarySpace is = new BinarySpace(attribute);
		attribute = new OTSDemographicAttribute<BooleanValue>(name, is, referentAttribute, 
				mapper.keySet().stream().collect(Collectors.toMap(
						key -> is.addValue(key), 
						key -> mapper.get(key).stream().map(val -> referentAttribute.getValueSpace().getValue(val))
							.collect(Collectors.toSet()))));
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
	public DemographicAttribute<OrderedValue> createOrderedAttribute(String name, GSCategoricTemplate ct){
		DemographicAttribute<OrderedValue> oa = null;
		oa = new DemographicAttribute<>(name, new OrderedSpace(oa, ct));
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
	public OTSDemographicAttribute<OrderedValue> createOrderedAgregatedAttribute(String name,
			GSCategoricTemplate gsCategoricTemplate, DemographicAttribute<OrderedValue> referentAttribute, 
			Map<String, Set<String>> mapper) {
		OTSDemographicAttribute<OrderedValue> attribute = null;
		OrderedSpace os = new OrderedSpace(attribute, gsCategoricTemplate);
		attribute = new OTSDemographicAttribute<OrderedValue>(name, os, referentAttribute, 
				mapper.keySet().stream().collect(Collectors.toMap(
						key -> os.addValue(key), 
						key -> mapper.get(key).stream().map(val -> referentAttribute.getValueSpace().getValue(val))
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
	public OTSDemographicAttribute<OrderedValue> createOrderedAgregatedAttribute(String name,
			DemographicAttribute<OrderedValue> referentAttribute, Map<String, Set<String>> mapper) {
		return this.createOrderedAgregatedAttribute(name, new GSCategoricTemplate(), referentAttribute, mapper);
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
	public <V extends IValue> STSDemographicAttribute<OrderedValue, V> createOrderedAttribute(String name,
			GSCategoricTemplate gsCategoricTemplate, DemographicAttribute<V> referentAttribute,
			Map<Set<String>, Set<String>> mapper) {
		STSDemographicAttribute<OrderedValue, V> attribute = null;
		OrderedSpace os = new OrderedSpace(attribute, gsCategoricTemplate);
		attribute = new STSDemographicAttribute<OrderedValue, V>(name, os, referentAttribute, 
				mapper.keySet().stream().collect(Collectors.toMap(
						key -> key.stream().map(val -> os.addValue(val))
							.collect(Collectors.toSet()), 
						key -> mapper.get(key).stream().map(val -> referentAttribute.getValueSpace().getValue(val))
							.collect(Collectors.toSet()))));
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
	public <V extends IValue> STSDemographicAttribute<OrderedValue, V> createOrderedAttribute(String name,
			DemographicAttribute<V> referentAttribute, Map<Set<String>, Set<String>> mapper) {
		return this.createOrderedAttribute(name, new GSCategoricTemplate(), referentAttribute, mapper);
	}
	
	/**
	 * Create ordered record value attribute
	 * 
	 * @param name
	 * @param gsCategoricTemplate
	 * @param referentAttribute
	 * @param mapper
	 * @return
	 */
	public <V extends IValue> OTODemographicAttribute<OrderedValue, V> createOrderedRecordAttribute(String name,
			GSCategoricTemplate gsCategoricTemplate,  DemographicAttribute<V> referentAttribute, 
			Map<String, String> mapper){
		OTODemographicAttribute<OrderedValue, V > attribute = null;
		OrderedSpace os = new OrderedSpace(attribute, gsCategoricTemplate);
		attribute = new OTODemographicAttribute<>(name, os, referentAttribute,
				mapper.keySet().stream().collect(Collectors.toMap(
						key -> os.getValue(key), 
						key -> referentAttribute.getValueSpace().getValue(mapper.get(key)))));
		return attribute;
	}
	
	/**
	 * Create ordered record value attribute
	 * 
	 * @param name
	 * @param gsCategoricTemplate
	 * @param referentAttribute
	 * @param mapper
	 * @return
	 */
	public <V extends IValue> OTODemographicAttribute<OrderedValue, V> createOrderedRecordAttribute(String name,
			DemographicAttribute<V> referentAttribute, Map<String, String> mapper){
		return this.createOrderedRecordAttribute(name, new GSCategoricTemplate(), referentAttribute, mapper);
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
		DemographicAttribute<NominalValue> na = null;
		na = new DemographicAttribute<>(name, new NominalSpace(na, ct));
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
	public OTSDemographicAttribute<NominalValue> createNominalAggregatedAttribute(String name,
			GSCategoricTemplate gsCategoricTemplate, DemographicAttribute<NominalValue> referentAttribute,
			Map<String, Set<String>> mapper) {
		OTSDemographicAttribute<NominalValue> attribute = null;
		NominalSpace ns = new NominalSpace(attribute, gsCategoricTemplate);
		attribute = new OTSDemographicAttribute<NominalValue>(name, ns, referentAttribute, 
				mapper.keySet().stream().collect(Collectors.toMap(
						key -> ns.addValue(key), 
						key -> mapper.get(key).stream().map(val -> referentAttribute.getValueSpace().getValue(val))
							.collect(Collectors.toSet()))));
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
	public OTSDemographicAttribute<NominalValue> createNominalAggregatedAttribute(String name,
			DemographicAttribute<NominalValue> referentAttribute,
			Map<String, Set<String>> mapper) {
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
	public <V extends IValue> STSDemographicAttribute<NominalValue, V> createNominalAttribute(String name,
			GSCategoricTemplate gsCategoricTemplate, DemographicAttribute<V> referentAttribute,
			Map<Set<String>, Set<String>> mapper) {
		STSDemographicAttribute<NominalValue, V> attribute = null;
		NominalSpace ns = new NominalSpace(attribute, gsCategoricTemplate);
		attribute = new STSDemographicAttribute<NominalValue, V>(name, ns, referentAttribute, 
				mapper.keySet().stream().collect(Collectors.toMap(
						key -> key.stream().map(val -> ns.addValue(val))
							.collect(Collectors.toSet()), 
						key -> mapper.get(key).stream().map(val -> referentAttribute.getValueSpace().getValue(val))
							.collect(Collectors.toSet()))));
		return attribute;
	}
	
	/**
	 * Create a nominal record value attribute
	 * <p>
	 * @see OTODemographicAttribute
	 * 
	 * @param string
	 * @param attCouple
	 * @param mapper
	 * @return
	 */
	public <V extends IValue> OTODemographicAttribute<NominalValue, V> createNominalRecordAttribute(String name, 
			GSCategoricTemplate gsCategoricTemplate, DemographicAttribute<V> referentAttribute,
			Map<String, String> mapper) {
		OTODemographicAttribute<NominalValue, V> attribute = null;
		NominalSpace ns = new NominalSpace(attribute, gsCategoricTemplate);
		attribute = new OTODemographicAttribute<>(name, ns, referentAttribute, 
				mapper.keySet().stream().collect(Collectors.toMap(
						key -> ns.addValue(key), 
						key -> referentAttribute.getValueSpace().getValue(mapper.get(key)))));
		return attribute;
	}
	
	/**
	 * Create a nominal record value attribute
	 * 
	 * @param name
	 * @param referentAttribute
	 * @param mapper
	 * @return
	 */
	public <V extends IValue> OTODemographicAttribute<NominalValue, V> createNominalRecordAttribute(String name, 
			DemographicAttribute<V> referentAttribute,
			Map<String, String> mapper) {
		return this.createNominalRecordAttribute(name, new GSCategoricTemplate(), referentAttribute, mapper);
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
		DemographicAttribute<RangeValue> ra = null;
		ra = new DemographicAttribute<RangeValue>(name, new RangeSpace(ra, rt));
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
	 * Create range aggregated value attribute
	 * 
	 * @param name
	 * @param gsCategoricTemplate
	 * @param referent
	 * @param mapper
	 * @return
	 */
	public OTSDemographicAttribute<RangeValue> createRangeAggregatedAttribute(String name,
			GSRangeTemplate rangeTemplate, DemographicAttribute<RangeValue> referentAttribute,
			Map<String, Set<String>> mapper) {
		OTSDemographicAttribute<RangeValue> attribute = null;
		RangeSpace rs = new RangeSpace(attribute, rangeTemplate);
		attribute = new OTSDemographicAttribute<RangeValue>(name, rs, referentAttribute, 
				mapper.keySet().stream().collect(Collectors.toMap(
						key -> rs.addValue(key), 
						key -> mapper.get(key).stream().map(val -> referentAttribute.getValueSpace().getValue(val))
							.collect(Collectors.toSet()))));
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
	public OTSDemographicAttribute<RangeValue> createRangeAggregatedAttribute(String name,
			DemographicAttribute<RangeValue> referentAttribute,
			Map<String, Set<String>> mapper) throws GSIllegalRangedData {
		return this.createRangeAggregatedAttribute(name, 
				new GSDataParser().getRangeTemplate(new ArrayList<>(mapper.keySet())), 
				referentAttribute, mapper);
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
	public <V extends IValue> STSDemographicAttribute<RangeValue, V> createRangeAttribute(String name,
			GSRangeTemplate rangeTemplate, DemographicAttribute<V> referentAttribute,
			Map<Set<String>, Set<String>> mapper) {
		STSDemographicAttribute<RangeValue, V> attribute = null;
		RangeSpace rs = new RangeSpace(attribute, rangeTemplate);
		attribute = new STSDemographicAttribute<>(name, rs, referentAttribute, 
				mapper.keySet().stream().collect(Collectors.toMap(
						key -> key.stream().map(val -> rs.addValue(val))
							.collect(Collectors.toSet()), 
						key -> mapper.get(key).stream().map(val -> referentAttribute.getValueSpace().getValue(val))
							.collect(Collectors.toSet()))));
		return attribute;
	}
	
}
