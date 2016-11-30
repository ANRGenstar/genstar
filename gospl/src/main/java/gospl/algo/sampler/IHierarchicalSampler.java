package gospl.algo.sampler;

import java.util.Collection;
import java.util.List;

import core.io.survey.attribut.ASurveyAttribute;
import core.io.survey.attribut.value.AValue;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.distribution.util.GosplBasicDistribution;

public interface IHierarchicalSampler extends ISampler<ACoordinate<ASurveyAttribute, AValue>> {


	public void setDistribution(
			GosplBasicDistribution gosplBasicDistribution,
			Collection<List<ASurveyAttribute>> explorationOrder);
	
	
}
