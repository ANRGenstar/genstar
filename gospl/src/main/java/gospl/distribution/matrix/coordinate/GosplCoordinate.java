package gospl.distribution.matrix.coordinate;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import core.io.survey.entity.attribut.AGenstarAttribute;
import core.io.survey.entity.attribut.value.AGenstarValue;

public class GosplCoordinate extends ACoordinate<AGenstarAttribute, AGenstarValue> {

	public GosplCoordinate(Set<AGenstarValue> coordinate) {
		super(coordinate);
	}

	@Override
	public Set<AGenstarAttribute> getDimensions() {
		return values().stream().map(AGenstarValue::getAttribute).collect(Collectors.toSet());
	}

	@Override
	public Map<AGenstarAttribute, AGenstarValue> getMap() {
		return values().stream().collect(Collectors.toMap(v -> v.getAttribute(), v -> v));
	}

	@Override
	protected boolean isCoordinateSetComplient(Set<AGenstarValue> coordinateSet) {
		Set<AGenstarAttribute> attributeSet = coordinateSet.stream().map(av -> av.getAttribute()).collect(Collectors.toSet());
		if(attributeSet.size() == coordinateSet.size())
			return true;
		return false;
	}

}
