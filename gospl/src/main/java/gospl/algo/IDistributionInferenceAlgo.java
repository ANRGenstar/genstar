package gospl.algo;

import core.metamodel.IValue;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationValue;
import gospl.algo.sampler.ISampler;
import gospl.distribution.exception.IllegalDistributionCreation;
import gospl.distribution.matrix.ASegmentedNDimensionalMatrix;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.entity.attribute.MappedAttribute;

public interface IDistributionInferenceAlgo<SamplerType extends ISampler<ACoordinate<APopulationAttribute, APopulationValue>>> {

	/**
	 * 
	 * WARNING: must step into 3 issues
	 * 
	 * <p><ul>
	 * 
	 * <li> For {@link ASegmentedNDimensionalMatrix} you must find a way to connect unrelated attributes (e.g. with estimation or with graphical models)
	 * 
	 * <li> For each {@link MappedAttribute} you must find and help to connect with the referent attribute {@link MappedAttribute#getReferentAttribute()}.
	 * It has more {@link IValue} and then has more information, so these hole should be filled (e.g. with empty attribute when there is no information at all 
	 * and estimation when information is partial)
	 * 
	 * </ul><p>
	 * 
	 * @param matrix
	 * @return
	 * @throws IllegalDistributionCreation
	 * @throws GosplSamplerException
	 */
	public ISampler<ACoordinate<APopulationAttribute, APopulationValue>> inferDistributionSampler(
			INDimensionalMatrix<APopulationAttribute, APopulationValue, Double> matrix, 
			SamplerType sampler) 
			throws IllegalDistributionCreation;
	
}
