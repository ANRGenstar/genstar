package gospl.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationValue;
import core.util.data.GSEnumDataType;
import core.util.excpetion.GSIllegalRangedData;
import core.util.random.GenstarRandom;
import gospl.GosplPopulation;
import gospl.entity.GosplEntity;
import gospl.entity.attribute.GosplAttributeFactory;
import gospl.entity.attribute.GSEnumAttributeType;

/**
 * 
 * Fully random generator: 
 * <p>
 * <ul>
 * <li> 1st constructor: lead to a fully random population (attribute & value 
 * are generated randomly from a set of chars)
 * </ul> 2nd constructor: given a set of attributes generate a population
 * <p>
 * Note: intended to be used as a population supplier on any test
 * 
 * @author kevinchapuis
 *
 */
public class UtilGenerator implements ISyntheticGosplPopGenerator {

	private int maxAtt;
	private int maxVal;

	char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();

	private Set<APopulationAttribute> attributes;

	Random random = GenstarRandom.getInstance();

	/**
	 * Set the maximum number of attribute and maximum value per attribute
	 * 
	 * @param maxAtt
	 * @param maxVal
	 */
	public UtilGenerator(int maxAtt, int maxVal) {
		this.maxAtt = maxAtt;
		this.maxVal = maxVal;
	}

	/**
	 * Provide the set of attribute to draw entity from
	 * 
	 * @param attributes
	 */
	public UtilGenerator(Set<APopulationAttribute> attributes){
		this.attributes = attributes;
	}

	@Override
	public GosplPopulation generate(int numberOfIndividual) {

		// Basic population to feed
		GosplPopulation gosplPop = new GosplPopulation();

		// Attribute Factory
		if(attributes == null){
			GosplAttributeFactory attF = new GosplAttributeFactory();
			this.attributes = IntStream.range(0, random.nextInt(maxAtt)+1)
					.mapToObj(i -> random.nextDouble() > 0.5 ? createStringAtt(attF) : createIntegerAtt(attF))
					.collect(Collectors.toSet());
		}

		IntStream.range(0, numberOfIndividual).forEach(i -> gosplPop.add(
				new GosplEntity(attributes.stream().collect(Collectors.toMap(att -> att, 
						att -> randomVal(att.getValues()))))));

		return gosplPop;
	}


	// ------------------------------------------------------ //
	// ---------- attribute & value random creator ---------- //
	// ------------------------------------------------------ //


	private APopulationAttribute createIntegerAtt(GosplAttributeFactory factory) {
		APopulationAttribute asa = null;
		try {
			asa = factory.createAttribute(generateName(random.nextInt(6)+1), 
					GSEnumDataType.Integer, 
					IntStream.range(0, maxVal).mapToObj(i -> String.valueOf(i)).collect(Collectors.toList()), 
					GSEnumAttributeType.unique);
		} catch (GSIllegalRangedData e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return asa;
	}

	private APopulationAttribute createStringAtt(GosplAttributeFactory factory){
		APopulationAttribute asa = null;
		try {
			asa = factory.createAttribute(generateName(random.nextInt(6)+1), 
					GSEnumDataType.String, 
					IntStream.range(0, random.nextInt(maxVal)).mapToObj(j -> 
					generateName(random.nextInt(j+1))).collect(Collectors.toList()), 
					GSEnumAttributeType.unique);
		} catch (GSIllegalRangedData e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return asa;
	}

	private String generateName(int size){
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < size; i++) {
			char c = chars[random.nextInt(chars.length)];
			sb.append(c);
		}
		return sb.toString();
	}

	// ---------------------- utilities ---------------------- //

	private APopulationValue randomVal(Set<APopulationValue> values){
		List<APopulationValue> vals = new ArrayList<>(values);
		return vals.get(random.nextInt(vals.size()));
	}

}
