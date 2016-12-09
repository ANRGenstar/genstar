package gospl.algo;

import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationValue;
import gospl.algo.sampler.ISampler;
import gospl.distribution.exception.IllegalDistributionCreation;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.entity.attribute.MappedAttribute;

public interface ISyntheticReconstructionAlgo<SamplerType extends ISampler<ACoordinate<APopulationAttribute, APopulationValue>>> {

	/**
	 * This method must provide a way to build a Synthetic Reconstructive (SR) sampler. SR is known in the literature
	 * as the method to generate synthetic population using probability distribution and monte carlo draws
	 * <p>
	 * WARNING: should provide answers to question like, how to deal with {@link MappedAttribute} & how to deal
	 * with limited information about relationship between attributes
	 * </p>
	 * @param matrix
	 * @return
	 * @throws IllegalDistributionCreation
	 * @throws GosplSamplerException
	 */
	public ISampler<ACoordinate<APopulationAttribute, APopulationValue>> inferSRSampler(
			INDimensionalMatrix<APopulationAttribute, APopulationValue, Double> matrix, 
			SamplerType sampler) 
			throws IllegalDistributionCreation;
	
}
