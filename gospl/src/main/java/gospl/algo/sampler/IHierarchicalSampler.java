package gospl.algo.sampler;

import java.util.Collection;
import java.util.List;

import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationValue;
import gospl.distribution.matrix.ASegmentedNDimensionalMatrix;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.distribution.util.GosplBasicDistribution;

public interface IHierarchicalSampler extends ISampler<ACoordinate<APopulationAttribute, APopulationValue>> {


	public void setDistribution(
			GosplBasicDistribution gosplBasicDistribution,
			Collection<List<APopulationAttribute>> explorationOrder, 
			ASegmentedNDimensionalMatrix<Double> segmentedMatrix
			);
	
	
}