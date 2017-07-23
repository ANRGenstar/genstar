package gospl.entity.attribute;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import core.metamodel.IAttribute;
import core.metamodel.IValue;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationValue;
import core.util.data.GSDataParser;
import core.util.data.GSEnumDataType;
import core.util.excpetion.GSIllegalRangedData;
import gospl.entity.attribute.value.RangeValue;
import gospl.entity.attribute.value.UniqueValue;

public class GosplAttributeFactory {

	private final GSDataParser parser;
	
	/*
	 * TODO: change to parameter 
	 * HINT: specific rule implementation
	 */
	private int minInt = 0;
	private int maxInt = 120;
	private double minDouble = 0d;
	private double maxDouble = 1d;

	public static GosplAttributeFactory getFactory() {
		return new GosplAttributeFactory();
	}

	public GosplAttributeFactory() {
		this.parser = new GSDataParser();
	}
	

	/**
	 * Main method to instantiate {@link APopulationAttribute}. Concrete type depend on {@link GSAttDataType} passed in argument.
	 * <p> <ul>
	 * <li>If {@code valueType.equals({@link GSEnumAttributeType#range})} then return a {@link RangeAttribute}
	 * <li>If {@code valueType.equals({@link GSEnumAttributeType#unique})} then return a {@link UniqueAttribute}
	 * <li>If {@code valueType.equals({@link GSEnumAttributeType#record})} then return a {@link RecordAttribute}
	 * <li>FutherMore if {@code !IAttribute#getReferentAttribute().equals(this) && !this.isRecordAttribute()} then return a {@link MappedAttribute} 
	 * </ul><p>
	 *   
	 * 
	 * @param nameOnData
	 * @param nameOnEntity
	 * @param dataType
	 * @param values
	 * @param attributeType
	 * 
	 * @return an {@link IAttribute}
	 * 
	 * @throws GSIllegalRangedData 
	 * @throws GSException
	 * @throws GenstarIllegalRangedData
	 */
	public APopulationAttribute createAttribute(String name, GSEnumDataType dataType, List<String> values,
			GSEnumAttributeType attributeType) throws GSIllegalRangedData {
		return createAttribute(name, dataType, values, attributeType, null, Collections.emptyMap());
	}

	/**
	 * Method that permit to instantiate specific case of {@link APopulationAttribute}: that is {@link MappedAttribute}
	 * <p>
	 * TODO: explain how
	 * 
	 * @param nameOnData
	 * @param nameOnEntity
	 * @param dataType
	 * @param values
	 * @param attributeType
	 * @param referentAttribute
	 * 
	 * @return an {@link IAttribute}
	 * 
	 * @throws GSException
	 * @throws GSIllegalRangedData 
	 * @throws GenstarIllegalRangedData
	 */
	public APopulationAttribute createAttribute(String name, GSEnumDataType dataType, List<String> values,
			GSEnumAttributeType attributeType, APopulationAttribute referentAttribute, Map<Set<String>, Set<String>> mapper) 
					throws GSIllegalRangedData {
		return createAttribute(name, dataType, values, values, attributeType, referentAttribute, mapper);
	}
	
	/**
	 * Instantiates attributes with pairs mapped data / model values
	 * 
	 * @param name
	 * @param dataType
	 * @param values
	 * @param attributeType
	 * @return
	 * @throws GSException
	 * @throws GSIllegalRangedData
	 */
	public APopulationAttribute createAttribute(String name, GSEnumDataType dataType, List<String> inputValues, 
			List<String> modelValues, GSEnumAttributeType attributeType) throws GSIllegalRangedData {
		return createAttribute(name, dataType, inputValues, modelValues, attributeType, null, Collections.emptyMap());
	}
	
	/**
	 * TODO: javadoc
	 * 
	 * @param name
	 * @param dataType
	 * @param inputValues
	 * @param modelValues
	 * @param attributeType
	 * @param referentAttribute
	 * @param mapper
	 * @return
	 * @throws GSIllegalRangedData
	 */
	public APopulationAttribute createAttribute(String name, GSEnumDataType dataType, List<String> inputValues, List<String> modelValues,
			GSEnumAttributeType attributeType, APopulationAttribute referentAttribute, Map<Set<String>, Set<String>> mapper) throws GSIllegalRangedData {
		APopulationAttribute att = null;
		switch (attributeType) {
		case unique:
			if(referentAttribute == null)
				att = new UniqueAttribute(name, dataType);
			else if (!mapper.isEmpty())
				att = new MappedAttribute(name, dataType, referentAttribute, mapper);
			else
				throw new IllegalArgumentException("cannot instantiate aggregated value without mapper");
			break;
		case range:
			if(mapper==null || mapper.isEmpty())
				att = new RangeAttribute(name, dataType);
			else if(referentAttribute != null)
				att = new MappedAttribute(name, dataType, referentAttribute, mapper);
			else
				throw new IllegalArgumentException("cannot instantiate aggregated value with "+referentAttribute+" referent attribute");
			break;
		case record:
			att = new RecordAttribute(name, dataType, referentAttribute);
			break;
		default:
			throw new IllegalArgumentException("The attribute meta data type "+attributeType+" is not applicable !");
		}
		att.setValues(this.getValues(attributeType, dataType, inputValues, modelValues, att));
		att.setEmptyValue(this.getEmptyValue(attributeType, dataType, att));
		return att;
	}

	/**
	 * create a value with {@code valueType}, concrete value type {@code dataType} and the given {@code attribute} to refer to.
	 * <p>
	 * {@code values} can represent an unlimited number of string values, only the first one for unique type and the two first
	 * ones for range type will be used for {@link AttributeValue} creation. if {@code values} is empty, then returned {@link AttributeValue}
	 * will be empty
	 * 
	 * @param {@link GSAttDataType} valueType
	 * @param {@link GSEnumDataType} dataType
	 * @param {@code List<String>} values
	 * @param {@link APopulationAttribute} attribute
	 * @return a value with {@link AttributeValue} type
	 * @throws GSException
	 * @throws GenstarIllegalRangedData
	 */
	public IValue createValue(GSEnumAttributeType valueType, GSEnumDataType dataType, List<String> inputValues, 
			List<String> modelValues, APopulationAttribute attribute) throws GSIllegalRangedData {
		if(inputValues.isEmpty())
			return getEmptyValue(valueType, dataType, attribute);
		return getValues(valueType, dataType, inputValues, modelValues, attribute).iterator().next();
	}

	// ----------------------------- Back office ----------------------------- //

	private Set<APopulationValue> getValues(GSEnumAttributeType attributeType, GSEnumDataType dataType, List<String> inputValues, 
			List<String> modelValues, APopulationAttribute attribute) throws GSIllegalRangedData{
		if(inputValues.size() != modelValues.size())
			throw new IllegalArgumentException("Attribute's value should not have divergent "
					+ "input ("+inputValues.size()+") and model ("+modelValues.size()+") value");
		Set<APopulationValue> vals = new HashSet<>();
		switch (attributeType) {
		case record:
			vals.add(new UniqueValue(modelValues.get(0), dataType, attribute));
			return vals;
		case unique:
			for(int i = 0; i < inputValues.size(); i++)
				if(dataType.isNumericValue())
					vals.add(new UniqueValue(inputValues.get(i).trim(), 
							parser.getNumber(modelValues.get(i).trim()).get(0), dataType, attribute));
				else
					vals.add(new UniqueValue(inputValues.get(i).trim(), modelValues.get(i).trim(), dataType, attribute));
			return vals;
		case range:
			if(dataType.equals(GSEnumDataType.Integer)){
				List<Integer> valList = new ArrayList<>();
				for(String range : modelValues)
					valList.addAll(parser.getRangedIntegerData(range, false));
				Collections.sort(valList);
				for(String val : modelValues){
					List<Integer> intVal = parser.getRangedIntegerData(val, false);
					if(intVal.size() == 1){
						if(intVal.get(0).equals(valList.get(0)))
							intVal.add(0, minInt);
						else
							intVal.add(maxInt);
					}
					vals.add(new RangeValue(intVal.get(0).toString(), intVal.get(1).toString(), 
							inputValues.get(modelValues.indexOf(val)), dataType, attribute));
				}
			} else if(dataType.equals(GSEnumDataType.Double)){
				List<Double> valList = new ArrayList<>();
				for(String range : modelValues)
					valList.addAll(parser.getRangedDoubleData(range, false));
				Collections.sort(valList);
				for(String val : modelValues){
					List<Double> doublVal = parser.getRangedDoubleData(val, false);
					if(doublVal.size() == 1){
						if(doublVal.get(0).equals(valList.get(0)))
							doublVal.add(0, minDouble);
						else
							doublVal.add(maxDouble);
					}
					vals.add(new RangeValue(doublVal.get(0).toString(), doublVal.get(1).toString(), 
							inputValues.get(modelValues.indexOf(val)), dataType, attribute));
				}
			}
			return vals;
		default:
			throw new IllegalStateException(attributeType+ " is not a valide type to construct a "+IValue.class.getCanonicalName());
		}
	}

	private APopulationValue getEmptyValue(GSEnumAttributeType attributeType, GSEnumDataType dataType, APopulationAttribute attribute) {
		switch (attributeType) {
		case unique:
			return new UniqueValue(dataType, attribute);
		case range:
			return new RangeValue(dataType, attribute);
		default:
			return new UniqueValue(dataType, attribute);
		}
	}

}
