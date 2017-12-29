package spll.popmapper.distribution;

import java.util.List;
import java.util.stream.Collectors;

import core.metamodel.entity.AGeoEntity;
import core.metamodel.value.IValue;
import core.util.random.GenstarRandom;
import spll.SpllEntity;
import spll.popmapper.constraint.SpatialConstraintMaxNumber;

public class NumberDensitySpatialDistribution extends AbstractSpatialDistribution{

	SpatialConstraintMaxNumber scNumber;
	

	public SpatialConstraintMaxNumber getScNumber() {
		return scNumber;
	}

	public void setScNumber(SpatialConstraintMaxNumber scNumber) {
		this.scNumber = scNumber;
	}
 
	public NumberDensitySpatialDistribution(SpatialConstraintMaxNumber scNumber) {
		super();
		this.scNumber = scNumber;
	}

	@Override
	public AGeoEntity<? extends IValue> getCandidate(SpllEntity entity, List<AGeoEntity<? extends IValue>> candidates) {
		List<Double> distribution = candidates.stream().map(a -> scNumber.getNestCapacities().get(a.getGenstarName()).doubleValue()).collect(Collectors.toList());
		distribution = normalizeDistribution(distribution);
		
		
		int index = randomChoice(distribution);
		if (index == -1) {
			return candidates.get(GenstarRandom.getInstance().nextInt(candidates.size()));
		}
		return candidates.get(index);
	}

	@Override
	public void releaseCache() {
		
		
	}
	
}
