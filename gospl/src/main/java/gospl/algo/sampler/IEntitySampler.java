package gospl.algo.sampler;

import java.util.Collection;

import core.metamodel.pop.APopulationEntity;
import gospl.distribution.matrix.AFullNDimensionalMatrix;

public interface IEntitySampler extends ISampler<APopulationEntity> {

	public void setSample(Collection<APopulationEntity> sample);

	public void setObjectives(AFullNDimensionalMatrix<Integer> process);
	
}
