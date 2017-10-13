package core.metamodel.pop.factory;

import java.util.List;

import core.metamodel.pop.DemographicAttribute;
import core.metamodel.value.IValue;
import core.metamodel.value.IValueSpace;
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
import core.util.data.GSDataParser;
import core.util.data.GSEnumDataType;
import core.util.data.GSRangeTemplate;
import core.util.excpetion.GSIllegalRangedData;

/**
 * TODO: javadoc
 * 
 * @author kevinchapuis
 *
 */
public class GosplAttributeFactory {
	
	private static GosplAttributeFactory gaf = new GosplAttributeFactory();
	
	private GosplAttributeFactory(){};
	
	public static GosplAttributeFactory getFactory() {
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

}
