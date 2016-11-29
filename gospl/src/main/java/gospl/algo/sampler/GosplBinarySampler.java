package gospl.algo.sampler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import core.io.survey.attribut.ASurveyAttribute;
import core.io.survey.attribut.value.AValue;
import core.util.GSPerformanceUtil;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.distribution.util.GosplBasicDistribution;

/**
 * Sample method to draw from a discrete distribution, based on binary search algorithm
 * <p>
 * Default random engine is {@link ThreadLocalRandom} current generator 
 * 
 * TODO: 
 * <ul> 
 *  <li> method reset sampler
 *  <li> random engine abstraction should extends another one than java {@link Random} 
 * </ul>
 * 
 * @author kevinchapuis
 *
 * @param <T>
 */
public class GosplBinarySampler extends GosplAbstractSampler {
	
	private static boolean DEBUG_SYSO = true;
	
	private List<ACoordinate<ASurveyAttribute, AValue>> indexedKey;
	private List<Double> indexedProbabilitySum;
		
	private final double EPSILON = Math.pow(10, -6);
	
	// -------------------- setup methods -------------------- //
	

	@Override
	public void setDistribution(GosplBasicDistribution distribution){
		GSPerformanceUtil gspu = new GSPerformanceUtil("Setup binary sample of size: "+
				distribution.size(), DEBUG_SYSO);
		gspu.sysoStempPerformance(0, this);
		this.indexedKey = new ArrayList<>(distribution.size());
		this.indexedProbabilitySum = new ArrayList<>(distribution.size());
		double sumOfProbabilities = 0d;
		int count = 1;
		for(Entry<ACoordinate<ASurveyAttribute, AValue>, Double> entry : distribution.entrySet()){
			indexedKey.add(entry.getKey());
			sumOfProbabilities += entry.getValue();
			indexedProbabilitySum.add(sumOfProbabilities);
			if(count++ % (distribution.size() / 10) == 0)
				gspu.sysoStempPerformance(count * 1d / distribution.size(), this);
		}
		if(Math.abs(sumOfProbabilities - 1d) > EPSILON)
			throw new IllegalArgumentException("Sum of probabilities for this sampler exceed 1 (SOP = "+sumOfProbabilities+")");
	}

	// -------------------- main contract -------------------- //
		
	@Override
	public ACoordinate<ASurveyAttribute, AValue> draw(){
		double rand = random.nextDouble();
		int floor = 0;
		int top = indexedKey.size() - 1;
		int mid;
		double midVal;
		while(floor < top){
			mid = (floor + top) / 2;
			midVal = indexedProbabilitySum.get(mid);
			if(rand < midVal) top = mid - 1;
			if(rand > midVal) floor = mid + 1;
			if(rand == midVal) return indexedKey.get((floor + top) / 2);
		}
		if(floor == top)
			if(indexedProbabilitySum.get(floor) <= rand && rand < indexedProbabilitySum.get(floor + 1))
				return indexedKey.get(floor); 
		if (floor - 1 == top)
			if(indexedProbabilitySum.get(top) <= rand && rand < indexedProbabilitySum.get(floor))
				return indexedKey.get(floor);
		throw new RuntimeException("Sample engine has not been able to draw one coordinate !!!\n"
				+ "random ("+rand+"), floor ("+floor+" = "+indexedProbabilitySum.get(floor)+") and top ("+top+" = "+indexedProbabilitySum.get(top)+") could not draw index\n"
						+ "befor floor is: "+indexedProbabilitySum.get(floor-1));
	}
	
	
	// -------------------- utility -------------------- //
	
	@Override
	public String toCsv(String csvSeparator){
		List<ASurveyAttribute> attributs = new ArrayList<>(indexedKey
				.parallelStream().flatMap(coord -> coord.getDimensions().stream())
				.collect(Collectors.toSet()));
		String s = String.join(csvSeparator, attributs.stream().map(att -> att.getAttributeName()).collect(Collectors.toList()));
		s += "; Probability\n";
		double formerProba = 0d;
		for(ACoordinate<ASurveyAttribute, AValue> coord : indexedKey){
			String line = "";
			for(ASurveyAttribute att : attributs){
				if(coord.getDimensions().contains(att)){
					if(line.isEmpty())
						line += coord.getMap().get(att).getStringValue();
					else
						line += csvSeparator + coord.getMap().get(att).getInputStringValue();
				} else {
					if(line.isEmpty())
						line += " ";
					else
						line += csvSeparator + " ";
				}
			}
			double actualProba = indexedProbabilitySum.get(indexedKey.indexOf(coord)) - formerProba; 
			formerProba = indexedProbabilitySum.get(indexedKey.indexOf(coord));
			s += line + csvSeparator + actualProba +"\n";
		}
		return s;
	}
	
}
