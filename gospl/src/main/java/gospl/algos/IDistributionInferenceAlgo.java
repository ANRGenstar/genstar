package gospl.algos;

import core.io.survey.attribut.ASurveyAttribute;
import core.io.survey.attribut.MappedAttribute;
import core.io.survey.attribut.value.AValue;
import core.metamodel.IValue;
import gospl.algos.exception.GosplSamplerException;
import gospl.algos.sampler.ISampler;
import gospl.distribution.exception.IllegalDistributionCreation;
import gospl.distribution.matrix.ASegmentedNDimensionalMatrix;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.distribution.matrix.coordinate.ACoordinate;

public interface IDistributionInferenceAlgo<D, A> {

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
	public ISampler<ACoordinate<ASurveyAttribute, AValue>> inferDistributionSampler(
			INDimensionalMatrix<ASurveyAttribute, AValue, Double> matrix, 
			ISampler<ACoordinate<ASurveyAttribute, AValue>> sampler) 
			throws IllegalDistributionCreation, GosplSamplerException;
	
}
