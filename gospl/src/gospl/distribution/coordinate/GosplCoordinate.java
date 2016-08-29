package gospl.distribution.coordinate;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import gospl.distribution.exception.MatrixCoordinateException;
import gospl.metamodel.attribut.IAttribute;
import gospl.metamodel.attribut.value.IValue;

public class GosplCoordinate extends ACoordinate<IAttribute, IValue> {

	public GosplCoordinate(Set<IValue> coordinate) throws MatrixCoordinateException {
		super(coordinate);
	}

	@Override
	public Set<IAttribute> getDimensions() {
		return values().stream().map(IValue::getAttribute).collect(Collectors.toSet());
	}

	@Override
	public Map<IAttribute, IValue> getMap() {
		return values().stream().collect(Collectors.toMap(v -> v.getAttribute(), v -> v));
	}

	@Override
	protected boolean isCoordinateSetComplient(Set<IValue> coordinateSet) {
		Set<IAttribute> attributeSet = coordinateSet.stream().map(av -> av.getAttribute()).collect(Collectors.toSet());
		if(attributeSet.size() == coordinateSet.size())
			return true;
		return false;
	}

}
