package gospl.sampler.sr;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import core.metamodel.attribute.demographic.DemographicAttribute;
import core.metamodel.value.IValue;
import core.util.GSPerformanceUtil;
import core.util.random.GenstarRandom;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.control.AControl;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.sampler.IDistributionSampler;

/**
 * Sample method to draw from a discrete distribution, based on binary search algorithm
 * <p> 
 * 
 * FIXME: 
 * <ul> 
 *  <li> method reset sampler
 *  <li> junit test because it is not robust at all
 * </ul>
 * 
 * @author kevinchapuis
 *
 * @param <T>
 */
public class GosplBinarySampler implements IDistributionSampler {
		
	private List<ACoordinate<DemographicAttribute<? extends IValue>, IValue>> indexedKey;
	private List<Double> indexedProbabilitySum;
	
	private final double EPSILON = Math.pow(10, -6);
	
	// -------------------- setup methods -------------------- //
	

	@Override
	public void setDistribution(AFullNDimensionalMatrix<Double> distribution){
		GSPerformanceUtil gspu = new GSPerformanceUtil("Setup binary sample of size: "+
				distribution.size());
		gspu.sysoStempPerformance(0, this);
		this.indexedKey = new ArrayList<>(distribution.size());
		this.indexedProbabilitySum = new ArrayList<>(distribution.size());
		double sumOfProbabilities = 0d;
		int count = 1;
		for(Entry<ACoordinate<DemographicAttribute<? extends IValue>, IValue>, AControl<Double>> entry : 
				distribution.getMatrix().entrySet()){
			indexedKey.add(entry.getKey());
			sumOfProbabilities += entry.getValue().getValue();
			indexedProbabilitySum.add(sumOfProbabilities);
			if(count++ % (distribution.size() / 10) == 0)
				gspu.sysoStempPerformance(count * 1d / distribution.size(), this);
		}
		if(Math.abs(sumOfProbabilities - 1d) > EPSILON)
			throw new IllegalArgumentException("Sum of probabilities for this sampler exceed 1 (SOP = "+sumOfProbabilities+")");
	}

	// -------------------- main contract -------------------- //
		
	@Override
	public ACoordinate<DemographicAttribute<? extends IValue>, IValue> draw(){
		double rand = GenstarRandom.getInstance().nextDouble();
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

	/**
	 * {@inheritDoc}
	 * <p>
	 * WARNING: make use of {@link Stream#parallel()}
	 */
	@Override
	public final List<ACoordinate<DemographicAttribute<? extends IValue>, IValue>> draw(int numberOfDraw) {
		return IntStream.range(0, numberOfDraw).parallel().mapToObj(i -> draw()).collect(Collectors.toList());
	}
		
	
	// -------------------- utility -------------------- //
	
	@Override
	public String toCsv(String csvSeparator){
		List<DemographicAttribute<? extends IValue>> attributs = new ArrayList<>(indexedKey
				.parallelStream().flatMap(coord -> coord.getDimensions().stream())
				.collect(Collectors.toSet()));
		String s = String.join(csvSeparator, attributs.stream().map(att -> att.getAttributeName()).collect(Collectors.toList()));
		s += "; Probability\n";
		double formerProba = 0d;
		for(ACoordinate<DemographicAttribute<? extends IValue>, IValue> coord : indexedKey){
			String line = "";
			for(DemographicAttribute<? extends IValue> att : attributs){
				if(coord.getDimensions().contains(att)){
					if(line.isEmpty())
						line += coord.getMap().get(att).getStringValue();
					else
						line += csvSeparator + coord.getMap().get(att).getStringValue();
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
