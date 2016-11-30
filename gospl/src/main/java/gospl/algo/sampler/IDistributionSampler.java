package gospl.algo.sampler;

import core.io.survey.attribut.ASurveyAttribute;
import core.io.survey.attribut.value.AValue;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.distribution.util.GosplBasicDistribution;

public interface IDistributionSampler extends ISampler<ACoordinate<ASurveyAttribute, AValue>> {

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
