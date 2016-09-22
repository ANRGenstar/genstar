package gospl.algos;

import gospl.algos.sampler.ISampler;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.metamodel.attribut.IAttribute;
import gospl.metamodel.attribut.value.IValue;

public class HierarchicalHypothesisAlgo implements IDistributionInferenceAlgo<IAttribute, IValue> {

	private boolean DEBUG_SYSO;

	public HierarchicalHypothesisAlgo(boolean DEBUG_SYSO) {
		
	}
	
	@Override
	public ISampler<ACoordinate<IAttribute, IValue>> inferDistributionSampler(
			INDimensionalMatrix<IAttribute, IValue, Double> matrix,
			ISampler<ACoordinate<IAttribute, IValue>> sampler) {
		
		return sampler;
	}

}
