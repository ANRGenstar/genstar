package gospl.algo.sampler;

import java.util.Collection;
import java.util.List;

import core.io.survey.entity.attribut.AGenstarAttribute;
import core.io.survey.entity.attribut.value.AGenstarValue;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.distribution.util.GosplBasicDistribution;

public interface IHierarchicalSampler extends ISampler<ACoordinate<AGenstarAttribute, AGenstarValue>> {


	public void setDistribution(
			GosplBasicDistribution gosplBasicDistribution,
			Collection<List<AGenstarAttribute>> explorationOrder, 
			ASegmentedNDimensionalMatrix<Double> segmentedMatrix
			);
	
	
}
