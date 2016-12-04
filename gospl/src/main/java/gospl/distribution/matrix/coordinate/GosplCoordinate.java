package gospl.distribution.matrix.coordinate;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationValue;

public class GosplCoordinate extends ACoordinate<APopulationAttribute, APopulationValue> {

	public GosplCoordinate(Set<APopulationValue> coordinate) {
		super(coordinate);
	}

	@Override
	public Set<APopulationAttribute> getDimensions() {
		return values().stream().map(APopulationValue::getAttribute).collect(Collectors.toSet());
	}

	@Override
	public Map<APopulationAttribute, APopulationValue> getMap() {
		return values().stream().collect(Collectors.toMap(v -> v.getAttribute(), v -> v));
	}

	@Override
	protected boolean isCoordinateSetComplient(Set<APopulationValue> coordinateSet) {
		Set<APopulationAttribute> attributeSet = coordinateSet.stream().map(av -> av.getAttribute()).collect(Collectors.toSet());
		if(attributeSet.size() == coordinateSet.size())
			return true;
		return false;
	}

}
