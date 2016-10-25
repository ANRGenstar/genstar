package gospl.distribution.matrix.coordinate;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import core.io.survey.attribut.ASurveyAttribute;
import core.io.survey.attribut.value.AValue;
import gospl.distribution.exception.MatrixCoordinateException;

public class GosplCoordinate extends ACoordinate<ASurveyAttribute, AValue> {

	public GosplCoordinate(Set<AValue> coordinate) throws MatrixCoordinateException {
		super(coordinate);
	}

	@Override
	public Set<ASurveyAttribute> getDimensions() {
		return values().stream().map(AValue::getAttribute).collect(Collectors.toSet());
	}

	@Override
	public Map<ASurveyAttribute, AValue> getMap() {
		return values().stream().collect(Collectors.toMap(v -> v.getAttribute(), v -> v));
	}

	@Override
	protected boolean isCoordinateSetComplient(Set<AValue> coordinateSet) {
		Set<ASurveyAttribute> attributeSet = coordinateSet.stream().map(av -> av.getAttribute()).collect(Collectors.toSet());
		if(attributeSet.size() == coordinateSet.size())
			return true;
		return false;
	}

}
