package core.metamodel.geo.attribute;

import core.metamodel.value.IValue;
import core.metamodel.value.binary.BinarySpace;
import core.metamodel.value.binary.BooleanValue;
import core.metamodel.value.categoric.NominalSpace;
import core.metamodel.value.categoric.NominalValue;
import core.metamodel.value.categoric.template.GSCategoricTemplate;
import core.metamodel.value.numeric.ContinuedSpace;
import core.metamodel.value.numeric.ContinuedValue;
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
			GeographicAttribute<ContinuedValue> cAtt = null;
			cAtt = new GeographicAttribute<ContinuedValue>(new GeographicValueSpace<>(new ContinuedSpace(cAtt)), name);
			attribute = cAtt; 
			break;
		case Boolean:
			GeographicAttribute<BooleanValue> bAtt = null;
			bAtt = new GeographicAttribute<BooleanValue>(new GeographicValueSpace<>(new BinarySpace(bAtt)), name);
			attribute = bAtt;
			break;
		default:
			GeographicAttribute<NominalValue> nAtt = null;
			nAtt = new GeographicAttribute<NominalValue>(new GeographicValueSpace<>(new NominalSpace(nAtt, new GSCategoricTemplate())), name);
			attribute = nAtt;
			break;
		}
		return attribute;
	}

	/**
	 * Create a integer value geographical attribute
	 * @param name
	 * @return
	 */
	public GeographicAttribute<? extends IValue> createIntegerAttribute(String name) {
		GeographicAttribute<IntegerValue> iAtt = null;
		iAtt = new GeographicAttribute<IntegerValue>(new GeographicValueSpace<>(new IntegerSpace(iAtt)), name);
		return iAtt;
	}
	
}
