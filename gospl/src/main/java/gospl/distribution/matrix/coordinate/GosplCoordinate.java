package gospl.distribution.matrix.coordinate;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationValue;

/**
 * <b>Warning: a GosplCoordinate should rarely, if never, be forged by a user himself. 
 * A coordinate is attached to a matrix. Its hash depends on the matrix which sets it.</b>
 * 
 * @author Kevin Chapuis
 *
 */
public class GosplCoordinate extends ACoordinate<APopulationAttribute, APopulationValue> {

	public GosplCoordinate(Set<APopulationValue> coordinate) {
		super(coordinate);
	}
	
	/**
	 * Convenience constructor to create a coordinate for a given matrix. 
	 * Pass parameters as string such as createCoordinate(matrix.getDimensions(), "gender", "homme", "age", "0-5"... ), 
	 * that is by alterning the name of the attribute and the value for the corresponding attribute.
	 * @param matrix
	 * @param values
	 * @return
	 */
	public static GosplCoordinate createCoordinate(Set<APopulationAttribute> attributes, String ... values) {
		
		Set<APopulationValue> coordinateValues = new HashSet<>();
		
		// collect all the attributes and index their names
		Map<String,APopulationAttribute> name2attribute = attributes.stream()
															.collect(Collectors.toMap(APopulationAttribute::getAttributeName,Function.identity()));

		if (values.length/2 != attributes.size()) {
			throw new IllegalArgumentException("you should pass pairs of attribute name and corresponding value, such as attribute 1 name, value for attribute 1, attribute 2 name, value for attribute 2...");
		}/*
		if (values.length % 2 != 0) {
			throw new IllegalArgumentException("values should be passed in even count, such as attribute 1 name, value for attribute 1, attribute 2 name, value for attribute 2...");
		}*/
		
		// lookup values
		for (int i=0; i<values.length; i=i+2) {
			final String attributeName = values[i];
			final String attributeValueStr = values[i+1];
			
			APopulationAttribute attribute = name2attribute.get(attributeName);
			if (attribute == null)
				throw new IllegalArgumentException("unknown attribute "+attributeName);
			coordinateValues.add(attribute.getValue(attributeValueStr)); // will raise exception if the value is not ok

		}
		
		return new GosplCoordinate(coordinateValues);
		
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
