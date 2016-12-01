package gospl.algo.sampler;

import core.io.survey.entity.attribut.AGenstarAttribute;
import core.io.survey.entity.attribut.value.AGenstarValue;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.distribution.util.GosplBasicDistribution;

public interface IDistributionSampler extends ISampler<ACoordinate<AGenstarAttribute, AGenstarValue>> {

	// ---------------- setup methods ---------------- //
	
	/**
	 * Set the distribution to draw within
	 * 
	 * @param distribution
	 */
	public void setDistribution(GosplBasicDistribution distribution);
	
	/**
	 * Set the distribution to draw within in form of a n-dimensional matrix
	 * 
	 * @param distribution
	 */
	public void setDistribution(AFullNDimensionalMatrix<Double> distribution);
	
}
