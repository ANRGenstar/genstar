package gospl.distribution;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationValue;
import core.metamodel.pop.io.GSSurveyType;
import core.util.data.GSDataParser;
import core.util.data.GSEnumDataType;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.control.AControl;
import gospl.distribution.matrix.control.ControlContingency;
import gospl.distribution.matrix.coordinate.ACoordinate;


/**
 * 
 * TODO: javadoc 
 * 
 * @author kevinchapuis
 *
 */
public class GosplContingencyTable extends AFullNDimensionalMatrix<Integer> {
	
	protected GosplContingencyTable(Map<APopulationAttribute, Set<APopulationValue>> dimensionAspectMap) {
		super(dimensionAspectMap, GSSurveyType.ContingencyTable);
	}
	
	protected GosplContingencyTable(Set<APopulationAttribute> attributes) {
		this(
				attributes.stream().collect(Collectors.toMap(Function.identity(),APopulationAttribute::getValues))
				);
	}

	
	
	// ----------------------- SETTER CONTRACT ----------------------- //

	
	@Override
	public boolean addValue(ACoordinate<APopulationAttribute, APopulationValue> coordinates, AControl<? extends Number> value){
		if(matrix.containsKey(coordinates))
			return false;
		return setValue(coordinates, value);
	}


	@Override
	public final boolean addValue(ACoordinate<APopulationAttribute, APopulationValue> coordinates, Integer value) {
		return addValue(coordinates, new ControlContingency(value));
	}
	
	@Override
	public boolean setValue(ACoordinate<APopulationAttribute, APopulationValue> coordinate, AControl<? extends Number> value){
		if(isCoordinateCompliant(coordinate)){
			coordinate.setHashIndex(matrix.size()+1+matrix.hashCode());
			matrix.put(coordinate, new ControlContingency(value.getValue().intValue()));
			return true;
		}
		return false;
	}

	@Override
	public final boolean setValue(ACoordinate<APopulationAttribute, APopulationValue> coordinate, Integer value) {
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
