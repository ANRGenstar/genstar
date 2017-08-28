package gospl.entity.attribute;

import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationValue;
import core.util.data.GSEnumDataType;
import gospl.entity.attribute.value.UniqueValue;

/**
 * TODO: javadoc
 * 
 * @author kevinchapuis
 * @author Duc an vo
 */
public class UniqueAttribute extends APopulationAttribute {

	public UniqueAttribute(String name, GSEnumDataType dataType) {
		super(name, dataType);
	}

	@Override
	public boolean isRecordAttribute() {
		return false;
	}


	@Override
	public APopulationValue getValue(String name) {
		
		APopulationValue res = getInputString2value().get(name);
		
		if (res == null) {
			// we are record, so we accept to create novel values on the fly 
			res = new UniqueValue(name.trim(), this.dataType, this);
			values.add(res);
			if (inputString2value != null)
				inputString2value.put(name, res);
		}
		
		return res;
	}

}
