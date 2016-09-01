package gospl.algos;

import gospl.algos.exception.GosplSamplerException;
import gospl.algos.sampler.ISampler;
import gospl.distribution.exception.IllegalDistributionCreation;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.ASegmentedNDimensionalMatrix;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.metamodel.attribut.AggregatedAttribute;
import gospl.metamodel.attribut.value.IValue;
import gospl.survey.GosplMetaDataType;

/**
 * Transpose a {@link INDimensionalMatrix} into a {@link ISampler}
 * 
 * @author kevinchapuis
 *
 * @param <D>
 * @param <A>
 */
public interface IDistributionInferenceAlgo<D, A> {

	/**
	 * 
	 * WARNING: must step into 3 issues
	 * 
	 * <p><ul>
	 * 
	 * <li> For each {@link AFullNDimensionalMatrix} you must unsure them to be of {@link GosplMetaDataType#GlobalFrequencyTable}. 
	 * It is straightforward from {@link GosplMetaDataType#ContingencyTable}, but less with {@link GosplMetaDataType#LocalFrequencyTable}
	 * because you have to find a referent contingent or global frequency to scale up the frame of reference (from local attribut to the overall population)
	 * 
	 * <li> For {@link ASegmentedNDimensionalMatrix} you must find a way to connect unrelated attributes (e.g. with estimation or with graphical models)
	 * 
	 * <li> For each {@link AggregatedAttribute} you must find and help to connect with the referent attribute {@link AggregatedAttribute#getReferentAttribute()}.
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
	public ISampler<ACoordinate<D, A>> inferDistributionSampler(INDimensionalMatrix<D, A, Double> matrix) 
			throws IllegalDistributionCreation, GosplSamplerException;

}
