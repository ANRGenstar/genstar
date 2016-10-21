package gospl.algos;

import gospl.algos.sampler.ISampler;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.distribution.matrix.coordinate.ACoordinate;
import io.metamodel.attribut.IAttribute;
import io.metamodel.attribut.value.IValue;
import io.util.GSPerformanceUtil;

public class HierarchicalHypothesisAlgo implements IDistributionInferenceAlgo<IAttribute, IValue> {

	private boolean DEBUG_SYSO;

	public HierarchicalHypothesisAlgo(boolean DEBUG_SYSO) {
		this.DEBUG_SYSO = DEBUG_SYSO;
	}
	
	@Override
	public ISampler<ACoordinate<IAttribute, IValue>> inferDistributionSampler(
			INDimensionalMatrix<IAttribute, IValue, Double> matrix,
			ISampler<ACoordinate<IAttribute, IValue>> sampler) {
		
		GSPerformanceUtil gspu = new GSPerformanceUtil("Compute hierachical sampler from conditional distribution\nTheoretical size = "+
				matrix.getDimensions().stream().mapToInt(d -> d.getValues().size()).reduce(1, (i1, i2) -> i1 * i2), DEBUG_SYSO);
		gspu.getStempPerformance(0);
			
		return sampler;
	}

}
