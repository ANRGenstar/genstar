package gospl.io.util;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import core.configuration.GenstarJsonUtil;
import core.configuration.dictionary.AttributeDictionary;
import core.configuration.dictionary.IGenstarDictionary;
import core.metamodel.attribute.Attribute;
import core.metamodel.attribute.AttributeFactory;
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
	public static IGenstarDictionary<Attribute<? extends IValue>> 
				readBayesianNetworkAsDictionary(CategoricalBayesianNetwork bn) {
		
		Collection<Attribute<? extends IValue>> attributes = new LinkedList<>();

		for (NodeCategorical n: bn.getNodes()) {
			
			final boolean isRange = detectIsRange(n.getDomain());
			final boolean isInteger = !isRange && detectIsInteger(n.getDomain());
			
			GSEnumDataType dataType = GSEnumDataType.Nominal;
			if (isRange) {
				dataType = GSEnumDataType.Range;
			} else if (isInteger) {
				dataType = GSEnumDataType.Integer;
			}
				
			Attribute<? extends IValue> att;
			try {
				att = AttributeFactory.getFactory().createAttribute(
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
		
		return new AttributeDictionary(attributes);
		
	}
	

	/**
	 * returns true if these modalities seem to be ranges
	 * @param modalities
	 * @return
	 */
	public static boolean detectIsRange(Collection<String> modalities) {
		
		if (modalities.size()<2)
			return false;
		
		System.out.println("is this a range? "+modalities);
		
		 // we might have <anything not number><several numbers><anything but numbers>
		Pattern oneNumber = Pattern.compile("[\\D]*\\d+[\\D]*"); 
		
		List<String> mods = new ArrayList<>(modalities);
		
		// if there is not one number in the first one, it's not a range
		if (!oneNumber.matcher(mods.get(0)).matches()) {
			System.out.println("no, because the first number is not a number: "+mods.get(0));
			return false;
		}
		
		// there should also be only one number in the last one
		if (!oneNumber.matcher(mods.get(mods.size()-1)).matches()) {
			System.out.println("no, because the last number is not a number: "+(mods.size()-1));
			return false;
		}
		
		// we might have <anything not number><several numbers><anything but numbers><several numbers><anything but numbers>
		Pattern twoNumbers = Pattern.compile("[\\D]*\\d+[\\D]+\\d+[\\D]*");

		// and then two numbers inbetween
		for (int i=1; i<mods.size()-2; i++) {
			if (!twoNumbers.matcher(mods.get(i)).matches()) {
				return false;
			}
			System.out.println("this might be a range:"+mods.get(i));
		}
		
		return true;
	}
	
	/**
	 * return true if the modalities are numeric.
	 * The detections works as: it is numeric if it is made of only numbers
	 * or there is exactly one number inside it.
	 * @param modalities
	 * @return
	 */
	public static boolean detectIsInteger(Collection<String> modalities) {

		Pattern oneNumber = Pattern.compile("[\\D]*\\d+[\\D]*"); 

		for (String s: modalities) {
			if (!oneNumber.matcher(s).matches())
				return false;
			/*try {
				Integer.parseInt(s);
			} catch (NumberFormatException e) {
				return false;
			}*/
		}
		return true;
	}
	

	
	private ReadDictionaryUtils() {}


	/**
	 * Reads a dictionnary in the Genstar JSON format.
	 * @param filename
	 * @return
	 */
	public static IGenstarDictionary<Attribute<? extends IValue>> readFromGenstarConfig(
			String filename) {
		
		GenstarJsonUtil sju = new GenstarJsonUtil();

		try {
			return sju.unmarshalFromGenstarJson(
					FileSystems.getDefault().getPath(filename), 
					AttributeDictionary.class);
		} catch (IllegalArgumentException | IOException e) {
			e.printStackTrace();
			throw new RuntimeException("error while reading the config file: "+e.getMessage(), e);
		}
		
	}

}
