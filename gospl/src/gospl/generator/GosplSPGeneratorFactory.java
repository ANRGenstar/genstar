package gospl.generator;

import gospl.algos.IDistributionInferenceAlgo;
import gospl.algos.exception.GosplSamplerException;
import gospl.distribution.exception.IllegalDistributionCreation;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.metamodel.attribut.IAttribute;
import gospl.metamodel.attribut.value.IValue;

/**
 * The factory that must be use to construct {@link ISyntheticPopGenerator}.
 * 
 * HINT: {@link #getGenerator(INDimensionalMatrix, IDistributionInferenceAlgo)} should be overload in order to create 
 * all sort of {@link ISyntheticPopGenerator}
 * 
 * @author kevinchapuis
 *
 */
public class GosplSPGeneratorFactory {
	
	public ISyntheticPopGenerator getGenerator(INDimensionalMatrix<IAttribute, IValue, Double> matrix, 
			IDistributionInferenceAlgo<IAttribute, IValue> distInfAlgo) throws GosplSamplerException, IllegalDistributionCreation{
		return new DistributionBasedGenerator(distInfAlgo.inferDistributionSampler(matrix));
	}
	
}
