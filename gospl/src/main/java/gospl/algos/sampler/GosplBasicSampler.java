package gospl.algos.sampler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import gospl.algos.exception.GosplSamplerException;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.metamodel.attribut.IAttribute;
import gospl.metamodel.attribut.value.IValue;

public class GosplBasicSampler implements ISampler<ACoordinate<IAttribute, IValue>> {

	private final List<ACoordinate<IAttribute, IValue>> indexedKey;
	private final List<Double> indexedProbabilitySum;

	private final Random random;

	private final double EPSILON = Math.pow(10, -6);

//	public GosplBasicSampler(INDimensionalMatrix<IAttribute, IValue, Double> distribution) throws GosplSamplerException{
//		this(ThreadLocalRandom.current(), distribution);
//	}
	
	public GosplBasicSampler(Map<ACoordinate<IAttribute, IValue>, Double> map) throws GosplSamplerException{
		this(ThreadLocalRandom.current(), map);
	}

//	public GosplBasicSampler(ThreadLocalRandom random,
//			INDimensionalMatrix<IAttribute, IValue, Double> distribution) throws GosplSamplerException {
//		this(random, distribution.getMatrix().entrySet()
//				.parallelStream().sorted(Map.Entry.comparingByValue((v1, v2) -> v1.compareTo(v2)))
//				.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().getValue())));
//	}
	
	public GosplBasicSampler(ThreadLocalRandom random,
			Map<ACoordinate<IAttribute, IValue>, Double> map) throws GosplSamplerException{
		this.random = random;
		this.indexedKey = new ArrayList<>(map.size());
		this.indexedProbabilitySum = new ArrayList<>(map.size());
		double sumOfProbabilities = 0d;
		for(Entry<ACoordinate<IAttribute, IValue>, Double> entry : map.entrySet()){
			indexedKey.add(entry.getKey());
			sumOfProbabilities += entry.getValue();
			indexedProbabilitySum.add(sumOfProbabilities);
		}
		if(Math.abs(sumOfProbabilities - 1d) > EPSILON)
			throw new GosplSamplerException("Sum of probabilities for this sampler exceed 1 (SOP = "+sumOfProbabilities+")");
	}

	@Override
	public ACoordinate<IAttribute, IValue> draw() throws GosplSamplerException {
		double rand = random.nextDouble();
		int idx = -1;
		for(double proba : indexedProbabilitySum){
			idx++;
			if(proba >= rand)
				return indexedKey.get(idx);
		}
		throw new GosplSamplerException("Sample engine has not been able to draw one coordinate !!!\n"
				+ "Max probability is: "+indexedProbabilitySum.get(indexedKey.size() - 1)+" and random double is: "+rand);
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
	public String toCsv(String csvSeparator) {
		List<IAttribute> attributs = new ArrayList<>(indexedKey
				.parallelStream().flatMap(coord -> coord.getDimensions().stream())
				.collect(Collectors.toSet()));
		String s = "Basic sampler: "+indexedKey.size()+" discret probabilities\n";
		s += String.join(csvSeparator, attributs.stream().map(att -> att.getName()).collect(Collectors.toList()))+"; Probability\n";
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
