package gospl.algo.sampler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import core.io.survey.entity.attribut.AGenstarAttribute;
import core.io.survey.entity.attribut.value.AGenstarValue;
import core.util.random.GenstarRandom;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.distribution.util.GosplBasicDistribution;


public class GosplBasicSampler implements IDistributionSampler {

	private List<ACoordinate<AGenstarAttribute, AGenstarValue>> indexedKey;
	private List<Double> indexedProbabilitySum;

	private final double EPSILON = Math.pow(10, -6);
	private final double EPS_ADJUST = Math.pow(10, -3);
	
	private double upperBoundRng = 1d;

	// -------------------- setup methods -------------------- //


	@Override
	public void setDistribution(GosplBasicDistribution distribution) {
		this.indexedKey = new ArrayList<>(distribution.size());
		this.indexedProbabilitySum = new ArrayList<>(distribution.size());
		double sumOfProbabilities = 0d;
		for(Entry<ACoordinate<AGenstarAttribute, AGenstarValue>, Double> entry : distribution.entrySet()){
			indexedKey.add(entry.getKey());
			sumOfProbabilities += entry.getValue();
			indexedProbabilitySum.add(sumOfProbabilities);
		}
		if(Math.abs(sumOfProbabilities - 1d) > EPSILON){
			// TODO: move to a BigDecimal distribution requirement
			if(Math.abs(sumOfProbabilities - 1d) < EPS_ADJUST){
				upperBoundRng = sumOfProbabilities;
			} else 
				throw new IllegalArgumentException("Sum of probabilities for this sampler is not equal to 1 (SOP = "+sumOfProbabilities+")");
		}
	}

	@Override
	public void setDistribution(AFullNDimensionalMatrix<Double> distribution) {
		this.setDistribution(new GosplBasicDistribution(distribution));
	}
	

	// -------------------- main contract -------------------- //
	
	@Override
	public ACoordinate<AGenstarAttribute, AGenstarValue> draw() {
		double rand = GenstarRandom.getInstance().nextDouble();
		while(rand > upperBoundRng)
			rand = GenstarRandom.getInstance().nextDouble();
		int idx = -1;
		for(double proba : indexedProbabilitySum){
			idx++;
			if(proba >= rand)
				return indexedKey.get(idx);
		}
		throw new RuntimeException("Sampler fail to draw an "+ACoordinate.class.getName()+" from the distribution:\n"
				+ "drawn random "+rand+" | probability bounds ["+indexedProbabilitySum.get(0)+" : "+indexedProbabilitySum.get(indexedProbabilitySum.size()-1)+"]");
	}


	/**
	 * {@inheritDoc}
	 * <p>
	 * WARNING: make use of {@link Stream#parallel()}
	 */
	@Override
	public final List<ACoordinate<AGenstarAttribute, AGenstarValue>> draw(int numberOfDraw) {
		return IntStream.range(0, numberOfDraw).parallel().mapToObj(i -> draw()).collect(Collectors.toList());
	}
		
	// -------------------- utility -------------------- //

	@Override
	public String toCsv(String csvSeparator) {
		List<AGenstarAttribute> attributs = new ArrayList<>(indexedKey
				.parallelStream().flatMap(coord -> coord.getDimensions().stream())
				.collect(Collectors.toSet()));
		String s = "Basic sampler: "+indexedKey.size()+" discret probabilities\n";
		s += String.join(csvSeparator, attributs.stream().map(att -> att.getAttributeName()).collect(Collectors.toList()))+"; Probability\n";
		double formerProba = 0d;
		for(ACoordinate<AGenstarAttribute, AGenstarValue> coord : indexedKey){
			String line = "";
			for(AGenstarAttribute att : attributs){
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
