package gospl.distribution;

import java.util.Map;
import java.util.Set;

import core.io.survey.attribut.ASurveyAttribute;
import core.io.survey.attribut.value.AValue;
import core.util.data.GSDataParser;
import core.util.data.GSEnumDataType;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.control.AControl;
import gospl.distribution.matrix.control.ControlContingency;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.metamodel.GSSurveyType;


/**
 * 
 * TODO: javadoc 
 * 
 * @author kevinchapuis
 *
 */
public class GosplContingencyTable extends AFullNDimensionalMatrix<Integer> {
	
	protected GosplContingencyTable(Map<ASurveyAttribute, Set<AValue>> dimensionAspectMap) {
		super(dimensionAspectMap, GSSurveyType.ContingencyTable);
	}
		
	// ----------------------- SETTER CONTRACT ----------------------- //

	
	@Override
	public boolean addValue(ACoordinate<ASurveyAttribute, AValue> coordinates, AControl<? extends Number> value){
		if(matrix.containsKey(coordinates))
			return false;
		return setValue(coordinates, value);
	}

	@Override
	public boolean setValue(ACoordinate<ASurveyAttribute, AValue> coordinate, AControl<? extends Number> value){
		if(isCoordinateCompliant(coordinate)){
			coordinate.setHashIndex(matrix.size()+1+matrix.hashCode());
			matrix.put(coordinate, new ControlContingency(value.getValue().intValue()));
			return true;
		}
		return false;
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
	public AControl<Integer> parseVal(GSDataParser parser, String val){
		if(!parser.getValueType(val).equals(GSEnumDataType.Integer))
			return getNulVal();
		return new ControlContingency(Integer.valueOf(val));
	}
				
}
