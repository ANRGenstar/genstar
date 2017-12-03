package core.metamodel.attribute.geographic;

import core.metamodel.value.IValue;
import core.metamodel.value.binary.BinarySpace;
import core.metamodel.value.binary.BooleanValue;
import core.metamodel.value.categoric.NominalSpace;
import core.metamodel.value.categoric.NominalValue;
import core.metamodel.value.categoric.template.GSCategoricTemplate;
import core.metamodel.value.numeric.ContinuousSpace;
import core.metamodel.value.numeric.ContinuousValue;
import core.metamodel.value.numeric.IntegerSpace;
import core.metamodel.value.numeric.IntegerValue;
import core.util.data.GSEnumDataType;

public class GeographicAttributeFactory {
	
	private static final GeographicAttributeFactory factory = new GeographicAttributeFactory();

	private GeographicAttributeFactory() {}
	
	/**
	 * Access to singleton factory
	 * 
	 * @return
	 */
	public static GeographicAttributeFactory getFactory() {
		return factory;
	}

	/**
	 * Main method to create any geographic attribute from minimal required input data
	 * @param name
	 * @param type
	 * @return
	 */
	public GeographicAttribute<? extends IValue> createAttribute(String name, GSEnumDataType type){
		GeographicAttribute<? extends IValue> attribute;
		switch (type) {
		case Integer:
			attribute = createIntegerAttribute(name);
			break;
		case Continue:
			attribute = createContinueAttribute(name);
			break;
		case Boolean:
			attribute = createBooleanAttribute(name);
			break;
		default:
			attribute = createNominalAttribute(name);
			break;
		}
		return attribute;
	}

	/**
	 * Create a non specified String value geographical attribute
	 * @param name
	 * @return
	 */
	public GeographicAttribute<NominalValue> createNominalAttribute(String name) {
		GeographicAttribute<NominalValue> nAtt = new GeographicAttribute<NominalValue>(name);
		nAtt.setValueSpace(new NominalSpace(nAtt, new GSCategoricTemplate()));
		return nAtt;
	}

	/**
	 * Create a boolean value geographical attribute
	 * @param name
	 * @return
	 */
	public GeographicAttribute<BooleanValue> createBooleanAttribute(String name) {
		GeographicAttribute<BooleanValue> bAtt = new GeographicAttribute<BooleanValue>(name);
		bAtt.setValueSpace(new BinarySpace(bAtt));
		return bAtt;
	}

	/**
	 * Create a integer value geographical attribute
	 * @param name
	 * @return
	 */
	public GeographicAttribute<IntegerValue> createIntegerAttribute(String name) {
		GeographicAttribute<IntegerValue> iAtt = new GeographicAttribute<IntegerValue>(name);
		iAtt.setValueSpace(new IntegerSpace(iAtt));
		return iAtt;
	}
	
	/**
	 * Create a continued value geographical attribute
	 * @param name
	 * @return
	 */
	public GeographicAttribute<ContinuousValue> createContinueAttribute(String name) {
		GeographicAttribute<ContinuousValue> cAtt = new GeographicAttribute<ContinuousValue>(name);
		cAtt.setValueSpace(new ContinuousSpace(cAtt));
		return cAtt;
	}
	
}
