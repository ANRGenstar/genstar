package gospl.algos.sampler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import core.io.survey.attribut.ASurveyAttribute;
import core.io.survey.attribut.value.AValue;
import gospl.ISampler;
import gospl.algos.exception.GosplSamplerException;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.distribution.util.BasicDistribution;


public class GosplBasicSampler implements ISampler<ACoordinate<ASurveyAttribute, AValue>> {

	private List<ACoordinate<ASurveyAttribute, AValue>> indexedKey;
	private List<Double> indexedProbabilitySum;

	private Random random = ThreadLocalRandom.current();

	private final double EPSILON = Math.pow(10, -6);

	// -------------------- setup methods -------------------- //

	@Override
	public void setRandom(Random random) {
		this.random = random;
	}

	@Override
	public void setDistribution(BasicDistribution distribution)
			throws GosplSamplerException {
		this.indexedKey = new ArrayList<>(distribution.size());
		this.indexedProbabilitySum = new ArrayList<>(distribution.size());
		double sumOfProbabilities = 0d;
		for(Entry<ACoordinate<ASurveyAttribute, AValue>, Double> entry : distribution.entrySet()){
			indexedKey.add(entry.getKey());
			sumOfProbabilities += entry.getValue();
			indexedProbabilitySum.add(sumOfProbabilities);
		}
		if(Math.abs(sumOfProbabilities - 1d) > EPSILON)
			throw new GosplSamplerException("Sum of probabilities for this sampler is not equal to 1 (SOP = "+sumOfProbabilities+")");
	}

	@Override
	public void setDistribution(AFullNDimensionalMatrix<Double> distribution) throws GosplSamplerException {
		this.setDistribution(new BasicDistribution(distribution));
	}

	// -------------------- main contract -------------------- //
	
	@Override
	public ACoordinate<ASurveyAttribute, AValue> draw() throws GosplSamplerException {
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
	public List<ACoordinate<ASurveyAttribute, AValue>> draw(int numberOfDraw) throws GosplSamplerException{
		return IntStream.range(0, numberOfDraw).parallel().mapToObj(i -> safeDraw()).collect(Collectors.toList());
	}
	
	private ACoordinate<ASurveyAttribute, AValue> safeDraw(){
		ACoordinate<ASurveyAttribute, AValue> draw = null;
		try {
			draw = draw();
		} catch (GosplSamplerException e) {
			e.printStackTrace();
		}
		return draw;
	}
	
	// -------------------- utility -------------------- //

	@Override
	public String toCsv(String csvSeparator) {
		List<ASurveyAttribute> attributs = new ArrayList<>(indexedKey
				.parallelStream().flatMap(coord -> coord.getDimensions().stream())
				.collect(Collectors.toSet()));
		String s = "Basic sampler: "+indexedKey.size()+" discret probabilities\n";
		s += String.join(csvSeparator, attributs.stream().map(att -> att.getAttributeName()).collect(Collectors.toList()))+"; Probability\n";
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
