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
import gospl.distribution.matrix.control.ControlFrequency;
import gospl.distribution.matrix.coordinate.ACoordinate;

/**
 * TODO: javadoc
 * 
 * @author kevinchapuis
 *
 */
public class GosplJointDistribution extends AFullNDimensionalMatrix<Double> {
	
	protected GosplJointDistribution(Map<ACoordinate<APopulationAttribute, APopulationValue>, AControl<Double>> matrix){
		super(matrix);
	}

	public GosplJointDistribution(Map<APopulationAttribute, Set<APopulationValue>> dimensionAspectMap, 
			GSSurveyType metaDataType) {
		super(dimensionAspectMap, metaDataType);
	}
	
	public GosplJointDistribution(Set<APopulationAttribute> attributes, GSSurveyType metaDataType) {
		this(
			attributes.stream().collect(Collectors.toMap(Function.identity(), APopulationAttribute::getValues)),
			metaDataType
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
	public final boolean addValue(ACoordinate<APopulationAttribute, APopulationValue> coordinates, Double value) {
		return addValue(coordinates, new ControlFrequency(value));
	}

	@Override
	public boolean setValue(ACoordinate<APopulationAttribute, APopulationValue> coordinate, AControl<? extends Number> value){
		if(isCoordinateCompliant(coordinate)){
			coordinate.setHashIndex(matrix.size()+1+matrix.hashCode());
			matrix.put(coordinate, new ControlFrequency(value.getValue().doubleValue()));
			return true;
		}
		return false;
	}
	

	@Override
	public final boolean setValue(ACoordinate<APopulationAttribute, APopulationValue> coordinate, Double value) {
		return setValue(coordinate, new ControlFrequency(value));
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
		if(parser.getValueType(val).equals(GSEnumDataType.String) || parser.getValueType(val).equals(GSEnumDataType.Boolean))
			return getNulVal();
		return new ControlFrequency(parser.getDouble(val));
	}

	@Override
	public void normalize() throws IllegalArgumentException {

		Double total = getVal().getValue();
		
		for (AControl<Double> c: getMatrix().values()) {
			c.multiply(1/total);
		}
			
	}
	
}
