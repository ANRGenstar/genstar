package spll.popmapper.distribution;

import java.util.ArrayList;
import java.util.List;

import core.util.random.GenstarRandom;

public abstract class AbstractSpatialDistribution implements ISpatialDistribution {


	
	List<Double> normalizeDistribution(List<Double> distribution) {
		List<Double> normalizedDistribution = new ArrayList<Double>();
		Double sumElt = 0.0;

		for (final Double elt : distribution) {
			sumElt = sumElt + elt;
		}
		
		for (int i = 0; i < distribution.size(); i++) {
			normalizedDistribution.add(distribution.get(i) / sumElt);
		}
		return normalizedDistribution;
	}
	
	int randomChoice(List<Double> normalizedDistribution) {
		double randomValue = GenstarRandom.getInstance().nextDouble();

		for (int i = 0; i < normalizedDistribution.size(); i++) {
			randomValue = randomValue - normalizedDistribution.get(i);
			if (randomValue <= 0) {
				return i;
			}
		}
		return -1;

	}

}
