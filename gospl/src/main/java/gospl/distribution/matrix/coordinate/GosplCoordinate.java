package gospl.distribution.matrix.coordinate;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import core.metamodel.attribute.Attribute;
import core.metamodel.value.IValue;

/**
 * <b>Warning: a GosplCoordinate should rarely, if never, be forged by a user himself. 
 * A coordinate is attached to a matrix. Its hash depends on the matrix which sets it.</b>
 * 
 * @author Kevin Chapuis
 *
 */
public class GosplCoordinate extends ACoordinate<Attribute<? extends IValue>, IValue> {

	public GosplCoordinate(Map<Attribute<? extends IValue>, IValue> coordinate) {
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
	public static GosplCoordinate createCoordinate(Set<Attribute<? extends IValue>> attributes, String ... values) {
		
		Map<Attribute<? extends IValue>, IValue> coordinateValues = new HashMap<>();
		
		// collect all the attributes and index their names
		Map<String,Attribute<? extends IValue>> name2attribute = attributes.stream()
															.collect(Collectors.toMap(Attribute::getAttributeName,Function.identity()));

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
			
			Attribute<? extends IValue> attribute = name2attribute.get(attributeName);
			if (attribute == null)
				throw new IllegalArgumentException("unknown attribute "+attributeName);
			coordinateValues.put(attribute, attribute.getValueSpace().getValue(attributeValueStr)); // will raise exception if the value is not ok

		}
		
		return new GosplCoordinate(coordinateValues);
		
	}

	@Override
	protected boolean isCoordinateSetComplient(Map<Attribute<? extends IValue>, IValue> coordinate) {
		return coordinate.entrySet().stream()
				.allMatch(e -> e.getValue().getValueSpace().getAttribute().equals(e.getKey()));
	}

}
