package gospl.algos.sampler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import gospl.algos.exception.GosplSamplerException;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.distribution.matrix.control.AControl;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.metamodel.attribut.IAttribute;
import gospl.metamodel.attribut.value.IValue;
import io.util.GSPerformanceUtil;

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
public class GosplBinarySampler implements ISampler<ACoordinate<IAttribute, IValue>> {
	
	private static final boolean DEBUG_SYSO = true;
	
	private final List<ACoordinate<IAttribute, IValue>> indexedKey;
	private final List<Double> indexedProbabilitySum;
	
	private final Random random;
	
	private final double EPSILON = Math.pow(10, -6);
	
	public GosplBinarySampler(INDimensionalMatrix<IAttribute, IValue, Double> distribution) throws GosplSamplerException {
		this(ThreadLocalRandom.current(), distribution);
	}
	
	public GosplBinarySampler(Random random, INDimensionalMatrix<IAttribute, IValue, Double> distribution) throws GosplSamplerException {
		GSPerformanceUtil gspu = new GSPerformanceUtil("Setup binary sample of size: "+
				distribution.size(), DEBUG_SYSO);
		gspu.sysoStempPerformance(0, this);
		this.random = random;
		this.indexedKey = new ArrayList<>(distribution.size());
		this.indexedProbabilitySum = new ArrayList<>(distribution.size());
		double sumOfProbabilities = 0d;
		Map<ACoordinate<IAttribute, IValue>, Double> sortedDistribution = distribution.getMatrix().entrySet()
				.parallelStream().sorted(Map.Entry.<ACoordinate<IAttribute, IValue>, AControl<Double>>comparingByValue())
				.collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getValue(),
                        (e1, e2) -> e1, LinkedHashMap::new));
		int count = 1;
		for(Entry<ACoordinate<IAttribute, IValue>, Double> entry : sortedDistribution.entrySet()){
			indexedKey.add(entry.getKey());
			sumOfProbabilities += entry.getValue();
			indexedProbabilitySum.add(sumOfProbabilities);
			if(count++ % (distribution.size() / 10) == 0)
				gspu.sysoStempPerformance(count * 1d / distribution.size(), this);
		}
		if(Math.abs(sumOfProbabilities - 1d) > EPSILON)
			throw new GosplSamplerException("Sum of probabilities for this sampler exceed 1 (SOP = "+sumOfProbabilities+")");
	}
		
	@Override
	public ACoordinate<IAttribute, IValue> draw() throws GosplSamplerException {
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
		throw new GosplSamplerException("Sample engine has not been able to draw one coordinate !!!\n"
				+ "random ("+rand+"), floor ("+floor+" = "+indexedProbabilitySum.get(floor)+") and top ("+top+" = "+indexedProbabilitySum.get(top)+") could not draw index\n"
						+ "befor floor is: "+indexedProbabilitySum.get(floor-1));
	}
	
	@Override
	public List<ACoordinate<IAttribute, IValue>> draw(int numberOfDraw) throws GosplSamplerException{
		// TODO: find a way to do it with streams and parallelism
		List<ACoordinate<IAttribute, IValue>> draws = new ArrayList<>();
		for(int i = 0; i < numberOfDraw; i++)
			draws.add(draw());
		return draws;
	}
	
	@Override
	public String toCsv(String csvSeparator){
		List<IAttribute> attributs = new ArrayList<>(indexedKey
				.parallelStream().flatMap(coord -> coord.getDimensions().stream())
				.collect(Collectors.toSet()));
		String s = String.join(csvSeparator, attributs.stream().map(att -> att.getName()).collect(Collectors.toList()));
		s += "; Probability\n";
		double formerProba = 0d;
		for(ACoordinate<IAttribute, IValue> coord : indexedKey){
			String line = "";
			for(IAttribute att : attributs){
				if(coord.getDimensions().contains(att)){
					if(line.isEmpty())
						line += coord.getMap().get(att).getInputStringValue();
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
