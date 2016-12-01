package gospl.distribution.matrix.coordinate;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import core.io.survey.entity.attribut.ASurveyAttribute;
import core.io.survey.entity.attribut.value.ASurveyValue;

public class GosplCoordinate extends ACoordinate<ASurveyAttribute, ASurveyValue> {

	public GosplCoordinate(Set<ASurveyValue> coordinate) {
		super(coordinate);
	}

	@Override
	public Set<ASurveyAttribute> getDimensions() {
		return values().stream().map(ASurveyValue::getAttribute).collect(Collectors.toSet());
	}

	@Override
	public Map<ASurveyAttribute, ASurveyValue> getMap() {
		return values().stream().collect(Collectors.toMap(v -> v.getAttribute(), v -> v));
	}

	@Override
	protected boolean isCoordinateSetComplient(Set<ASurveyValue> coordinateSet) {
		Set<ASurveyAttribute> attributeSet = coordinateSet.stream().map(av -> av.getAttribute()).collect(Collectors.toSet());
		if(attributeSet.size() == coordinateSet.size())
			return true;
		return false;
	}

}
