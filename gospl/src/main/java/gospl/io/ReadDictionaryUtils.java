package gospl.io;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import core.metamodel.pop.attribute.DemographicAttribute;
import core.metamodel.pop.attribute.DemographicAttributeFactory;
import core.metamodel.value.IValue;
import core.util.data.GSEnumDataType;
import core.util.excpetion.GSIllegalRangedData;
import gospl.algo.sr.bn.CategoricalBayesianNetwork;
import gospl.algo.sr.bn.NodeCategorical;

/**
 * Provides tools to read dictionaries from various file formats 
 * 
 * @author Samuel Thiriot
 */
public class ReadDictionaryUtils {

	/**
	 * Loads variables from a Bayesian network, using their values as a self-description
	 * of their dictionary.
	 * 
	 * TODO detect ranges
	 * 
	 * @param bn
	 * @return
	 */
	public static Collection<DemographicAttribute<? extends IValue>> readBayesianNetworkAsDictionary(CategoricalBayesianNetwork bn) {
		
		Collection<DemographicAttribute<? extends IValue>> attributes = new LinkedList<>();

		for (NodeCategorical n: bn.getNodes()) {
			
			final boolean isRange = detectIsRange(n.getDomain());
			final boolean isInteger = !isRange && detectIsInteger(n.getDomain());
			
			GSEnumDataType dataType = GSEnumDataType.Nominal;
			if (isRange) {
				dataType = GSEnumDataType.Range;
			} else if (isInteger) {
				dataType = GSEnumDataType.Integer;
			}
				
			DemographicAttribute<? extends IValue> att;
			try {
				att = DemographicAttributeFactory.getFactory().createAttribute(
						n.getName(), 
						dataType,
						new ArrayList<String>(n.getDomain())
						);
				attributes.add(att);
			} catch (GSIllegalRangedData e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			
		}
		
		return attributes;
		
	}
	

	/**
	 * returns true if these modalities seem to be ranges
	 * @param modalities
	 * @return
	 */
	public static boolean detectIsRange(Collection<String> modalities) {
		
		if (modalities.size()<2)
			return false;
		
		Pattern oneNumber = Pattern.compile("[\\D]*\\d+[\\D]*");
		
		List<String> mods = new ArrayList<>(modalities);
		
		// if there is not one number in the first one, it's not a range
		if (!oneNumber.matcher(mods.get(0)).matches())
			return false;
		
		// there should also be only one number in the last one
		if (!oneNumber.matcher(mods.get(mods.size()-1)).matches())
			return false;
		
		Pattern twoNumbers = Pattern.compile("[\\D]*\\d+[\\D]*\\d+[\\D]*");

		// and then two numbers inbetween
		for (int i=1; i<mods.size()-2; i++) {
			if (!twoNumbers.matcher(mods.get(i)).matches())
				return false;
		}
		
		return true;
	}
	
	/**
	 * return true if the modalities are numeric
	 * @param modalities
	 * @return
	 */
	public static boolean detectIsInteger(Collection<String> modalities) {
		for (String s: modalities) {
			try {
				Integer.parseInt(s);
			} catch (NumberFormatException e) {
				return false;
			}
		}
		return true;
	}
	

	
	private ReadDictionaryUtils() {}

}
