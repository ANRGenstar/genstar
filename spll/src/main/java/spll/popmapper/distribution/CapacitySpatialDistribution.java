package spll.popmapper.distribution;

import java.util.List;
import java.util.stream.Collectors;

import core.metamodel.entity.AGeoEntity;
import core.metamodel.value.IValue;
import core.util.random.roulette.RouletteWheelSelectionFactory;
import spll.SpllEntity;
import spll.popmapper.constraint.SpatialConstraintMaxNumber;

/**
 * Draw spatial entity based on capacity associated with each candidate. Provided capacity is based on
 * {@link SpatialConstraintMaxNumber} 
 * 
 * @author kevinchapuis
 *
 */
public class CapacitySpatialDistribution implements ISpatialDistribution {

	SpatialConstraintMaxNumber scNumber;
 
	public CapacitySpatialDistribution(SpatialConstraintMaxNumber scNumber) {
		super();
		this.scNumber = scNumber;
	}
	
	@Override
	public AGeoEntity<? extends IValue> getCandidate(SpllEntity entity, List<AGeoEntity<? extends IValue>> candidates) {
		return candidates.get(RouletteWheelSelectionFactory.getRouletteWheel(candidates.stream()
				.map(a -> scNumber.getNestCapacities().get(a.getGenstarName()).doubleValue())
				.collect(Collectors.toList()))
			.drawIndex());
	}
	
	/**
	 * 
	 * @return
	 */
	public SpatialConstraintMaxNumber getScNumber() {
		return scNumber;
	}

	/**
	 * 
	 * @param scNumber
	 */
	public void setScNumber(SpatialConstraintMaxNumber scNumber) {
		this.scNumber = scNumber;
	}
	
}
