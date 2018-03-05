package gospl.algo.sr;

import core.metamodel.attribute.Attribute;
import core.metamodel.value.IValue;
import gospl.distribution.exception.IllegalDistributionCreation;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.sampler.ISampler;

public interface ISyntheticReconstructionAlgo<SamplerType extends ISampler<ACoordinate<Attribute<? extends IValue>, IValue>>> {

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
	public ISampler<ACoordinate<Attribute<? extends IValue>, IValue>> inferSRSampler(
			INDimensionalMatrix<Attribute<? extends IValue>, IValue, Double> matrix, 
			SamplerType sampler) 
			throws IllegalDistributionCreation;
	
}
