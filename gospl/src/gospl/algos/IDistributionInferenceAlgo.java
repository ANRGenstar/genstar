package gospl.algos;

import gospl.algos.exception.GosplSamplerException;
import gospl.algos.sampler.ISampler;
import gospl.distribution.exception.IllegalDistributionCreation;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.distribution.matrix.coordinate.ACoordinate;

public interface IDistributionInferenceAlgo<D, A> {

	public ISampler<ACoordinate<D, A>> inferDistributionSampler(INDimensionalMatrix<D, A, Double> matrix) 
			throws IllegalDistributionCreation, GosplSamplerException;
	
}
