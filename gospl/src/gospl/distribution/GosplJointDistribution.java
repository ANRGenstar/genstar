package gospl.distribution;

import java.util.Map;
import java.util.Set;

import gospl.distribution.control.AControl;
import gospl.distribution.control.ControlFrequency;
import gospl.distribution.exception.MatrixCoordinateException;
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

	public GosplJointDistribution(Map<IAttribute, Set<IValue>> dimensionAspectMap, GosplMetatDataType metaDataType) throws MatrixCoordinateException {
		super(dimensionAspectMap, metaDataType);
	}
		
	// ----------------------- SETTER CONTRACT ----------------------- //
	
	/*
	@Override
	public boolean addValue(ACoordinate<AbstractAttribute, AttributeValue> coordinates, AControl<? extends Number> value){
		if(matrix.containsKey(coordinates))
			return false;
		return setValue(coordinates, value);
	}

	@Override
	public boolean setValue(ACoordinate<AbstractAttribute, AttributeValue> coordinate, AControl<? extends Number> value){
		if(isCoordinateCompliant(coordinate)){
			coordinate.setHashIndex(matrix.size()+1+matrix.hashCode());
			matrix.put(coordinate, new ControlFrequency(value.getValue().doubleValue()));
			return true;
		}
		return false;
	}
	*/
	
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
