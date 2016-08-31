package gospl.distribution;

import java.util.Map;
import java.util.Set;

import gospl.distribution.exception.MatrixCoordinateException;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.control.AControl;
import gospl.distribution.matrix.control.ControlFrequency;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.metamodel.attribut.IAttribute;
import gospl.metamodel.attribut.value.IValue;
import gospl.survey.GosplMetatDataType;
import io.data.GSDataParser;
import io.data.GSDataType;

/**
 * TODO: javadoc
 * 
 * @author kevinchapuis
 *
 */
public class GosplJointDistribution extends AFullNDimensionalMatrix<Double> {

	protected GosplJointDistribution(Map<IAttribute, Set<IValue>> dimensionAspectMap, GosplMetatDataType metaDataType) throws MatrixCoordinateException {
		super(dimensionAspectMap, metaDataType);
	}
		
	// ----------------------- SETTER CONTRACT ----------------------- //
	
	
	@Override
	public boolean addValue(ACoordinate<IAttribute, IValue> coordinates, AControl<? extends Number> value){
		if(matrix.containsKey(coordinates))
			return false;
		return setValue(coordinates, value);
	}

	@Override
	public boolean setValue(ACoordinate<IAttribute, IValue> coordinate, AControl<? extends Number> value){
		if(isCoordinateCompliant(coordinate)){
			coordinate.setHashIndex(matrix.size()+1+matrix.hashCode());
			matrix.put(coordinate, new ControlFrequency(value.getValue().doubleValue()));
			return true;
		}
		return false;
	}
	
	// ----------------------- CONTRACT ----------------------- //
	
	@Override
	public AControl<Double> getNulVal() {
		return new ControlFrequency(0d);
	}
	

	@Override
	public AControl<Double> getIdentityProductVal() {
		return new ControlFrequency(1d);
	}

	@Override
	public AControl<Double> parseVal(GSDataParser parser, String val) {
		if(parser.getValueType(val).equals(GSDataType.String) || parser.getValueType(val).equals(GSDataType.Boolean))
			return getNulVal();
		return new ControlFrequency(parser.getDouble(val));
	}
	
}
