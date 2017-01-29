package gospl.algo.sampler;

import java.util.Collection;
import java.util.List;

import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationValue;
import gospl.algo.sampler.evaluation.IEvaluableSampler;
import gospl.distribution.matrix.ASegmentedNDimensionalMatrix;
import gospl.distribution.matrix.coordinate.ACoordinate;

public interface IHierarchicalSampler extends ISampler<ACoordinate<APopulationAttribute, APopulationValue>>,
												IEvaluableSampler {


	public void setDistribution(
			Collection<List<APopulationAttribute>> explorationOrder, 
			ASegmentedNDimensionalMatrix<Double> segmentedMatrix
			);
	
	
}
