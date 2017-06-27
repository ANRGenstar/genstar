package gospl.sampler;

import java.util.Collection;
import java.util.Set;

import core.metamodel.pop.APopulationEntity;
import gospl.distribution.matrix.AFullNDimensionalMatrix;

public interface IEntitySampler extends ISampler<APopulationEntity> {

	public void setSample(Collection<APopulationEntity> sample);

	public void addObjectives(AFullNDimensionalMatrix<Integer> objectives);
	
	public Set<APopulationEntity> drawUnique(int numberOfDraw);
	
}
