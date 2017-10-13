package gospl.sampler;

import core.metamodel.pop.DemographicAttribute;
import core.metamodel.value.IValue;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.coordinate.ACoordinate;

public interface IDistributionSampler extends ISampler<ACoordinate<DemographicAttribute<? extends IValue>, IValue>> {

	// ---------------- setup methods ---------------- //
	
	/**
	 * Set the distribution to draw within in form of a n-dimensional matrix
	 * 
	 * @param distribution
	 */
	public void setDistribution(AFullNDimensionalMatrix<Double> distribution);
	
}
