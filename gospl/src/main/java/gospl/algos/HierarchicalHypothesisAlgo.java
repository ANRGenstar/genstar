package gospl.algos;

import core.io.survey.attribut.ASurveyAttribute;
import core.io.survey.attribut.value.AValue;
import core.util.GSPerformanceUtil;
import gospl.IDistributionInferenceAlgo;
import gospl.INDimensionalMatrix;
import gospl.ISampler;
import gospl.distribution.matrix.coordinate.ACoordinate;

public class HierarchicalHypothesisAlgo implements IDistributionInferenceAlgo<ASurveyAttribute, AValue> {

	private boolean DEBUG_SYSO;

	public HierarchicalHypothesisAlgo(boolean DEBUG_SYSO) {
		this.DEBUG_SYSO = DEBUG_SYSO;
	}
	
	@Override
	public ISampler<ACoordinate<ASurveyAttribute, AValue>> inferDistributionSampler(
			INDimensionalMatrix<ASurveyAttribute, AValue, Double> matrix,
			ISampler<ACoordinate<ASurveyAttribute, AValue>> sampler) {
		
		GSPerformanceUtil gspu = new GSPerformanceUtil("Compute hierachical sampler from conditional distribution\nTheoretical size = "+
				matrix.getDimensions().stream().mapToInt(d -> d.getValues().size()).reduce(1, (i1, i2) -> i1 * i2), DEBUG_SYSO);
		gspu.getStempPerformance(0);
		
		// TODO: algo
		// First define a standard hierarchical setup (e.g. sorted map, custom object)
			
		return sampler;
	}

}
