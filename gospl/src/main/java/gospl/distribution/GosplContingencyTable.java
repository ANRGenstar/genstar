package gospl.distribution;

import java.util.Set;

import core.metamodel.attribute.Attribute;
import core.metamodel.io.GSSurveyType;
import core.metamodel.value.IValue;
import core.util.data.GSDataParser;
import core.util.data.GSEnumDataType;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.control.AControl;
import gospl.distribution.matrix.control.ControlContingency;
import gospl.distribution.matrix.coordinate.ACoordinate;


/**
 * Complete n dimensional matrix with contingency cell, which means internal storage data are integers.
 * 
 * @see AFullNDimensionalMatrix
 * 
 * @author kevinchapuis
 *
 */
public class GosplContingencyTable extends AFullNDimensionalMatrix<Integer> {
	
	public GosplContingencyTable(Set<Attribute<? extends IValue>> attributes) {
		super(attributes, GSSurveyType.ContingencyTable);
	}
		
	
	// ----------------------- SETTER CONTRACT ----------------------- //

	
	@Override
	public boolean addValue(ACoordinate<Attribute<? extends IValue>, IValue> coordinates, AControl<? extends Number> value){
		if(matrix.containsKey(coordinates))
			return false;
		return setValue(coordinates, value);
	}


	@Override
	public final boolean addValue(ACoordinate<Attribute<? extends IValue>, IValue> coordinates, Integer value) {
		return addValue(coordinates, new ControlContingency(value));
	}
	
	@Override
	public boolean setValue(ACoordinate<Attribute<? extends IValue>, IValue> coordinate, AControl<? extends Number> value){
		if(isCoordinateCompliant(coordinate)){
			coordinate.setHashIndex(matrix.size());
			matrix.put(coordinate, new ControlContingency(value.getValue().intValue()));
			return true;
		}
		return false;
	}

	@Override
	public final boolean setValue(ACoordinate<Attribute<? extends IValue>, IValue> coordinate, Integer value) {
		return setValue(coordinate, new ControlContingency(value));
	}
	

	// ----------------------- SIDE CONTRACT ----------------------- //
	
	@Override
	public AControl<Integer> getNulVal() {
		return new ControlContingency(0);
	}
	
	@Override
	public AControl<Integer> getIdentityProductVal() {
		return new ControlContingency(1);
	}
	
	@Override
	public AControl<Integer> getAtomicVal() {
		return new ControlContingency(1);
	}
	
	@Override
	public AControl<Integer> parseVal(GSDataParser parser, String val){
		if(!parser.getValueType(val).equals(GSEnumDataType.Integer))
			return getNulVal();
		return new ControlContingency(Integer.valueOf(val));
	}

	@Override
	public boolean checkGlobalSum() {
		// always true
		return true;
	}


	@Override
	public void normalize() throws IllegalArgumentException {

		throw new IllegalArgumentException("should not normalize a "+getMetaDataType());		
		
	}


}
