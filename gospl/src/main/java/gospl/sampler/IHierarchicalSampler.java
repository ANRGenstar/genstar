package gospl.sampler;

import java.util.Collection;
import java.util.List;

import core.metamodel.pop.attribute.DemographicAttribute;
import core.metamodel.value.IValue;
import gospl.distribution.matrix.ASegmentedNDimensionalMatrix;
import gospl.distribution.matrix.coordinate.ACoordinate;

public interface IHierarchicalSampler extends ISampler<ACoordinate<DemographicAttribute<? extends IValue>, IValue>> {


	public void setDistribution(
			Collection<List<DemographicAttribute<? extends IValue>>> explorationOrder, 
			ASegmentedNDimensionalMatrix<Double> segmentedMatrix
			);
	
}
