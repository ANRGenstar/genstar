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
import gospl.entity.attribute.AttributeFactory;
import gospl.entity.attribute.GSEnumAttributeType;

/**
 * 
 * Fully random generator: attribute and they values are randomly init. i.e. number of attribute,
 * number of value for each attribute, attribute name and value are choose randomly <p>
 * 
 * Use intended to supply any localization and / or interaction
 * 
 * @author kevinchapuis
 *
 */
public class UniformRandomGenerator implements ISyntheticGosplPopGenerator {

	private int maxAtt;
	private int maxVal;
	
	char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
	Random random = GenstarRandom.getInstance();

	public UniformRandomGenerator(int maxAtt, int maxVal) {
		this.maxAtt = maxAtt;
		this.maxVal = maxVal;
	}
	
	@Override
	public GosplPopulation generate(int numberOfIndividual) {
		
		// Basic population to feed
		GosplPopulation gosplPop = new GosplPopulation();
		
		// Attribute Factory
		AttributeFactory attF = new AttributeFactory();
		Set<APopulationAttribute> attSet = IntStream.range(0, random.nextInt(maxAtt)+1)
				.mapToObj(i -> random.nextDouble() > 0.5 ? createStringAtt(attF) : createIntegerAtt(attF))
				.collect(Collectors.toSet());
		
		IntStream.range(0, numberOfIndividual).forEach(i -> gosplPop.add(
				new GosplEntity(attSet.stream().collect(Collectors.toMap(att -> att, 
						att -> randomVal(att.getValues()))))));
		
		return gosplPop;
	}

	private APopulationAttribute createIntegerAtt(AttributeFactory factory) {
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
	
	private APopulationAttribute createStringAtt(AttributeFactory factory){
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
	
	private APopulationValue randomVal(Set<APopulationValue> values){
		List<APopulationValue> vals = new ArrayList<>(values);
		return vals.get(random.nextInt(vals.size()));
	}

}
