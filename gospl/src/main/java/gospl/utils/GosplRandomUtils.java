package gospl.utils;

import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationValue;
import core.util.random.GenstarRandomUtils;

public class GosplRandomUtils {

	private GosplRandomUtils() {};
	
	/**
	 * returns one value in the values of this attribute
	 * @param a
	 * @return
	 */
	public static APopulationValue oneOf(APopulationAttribute a) {
		
		return GenstarRandomUtils.oneOf(a.getValues());
				
	}

}
