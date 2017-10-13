package gospl.sampler;

import java.util.Collection;
import java.util.Set;

import core.metamodel.pop.ADemoEntity;
import gospl.distribution.matrix.AFullNDimensionalMatrix;

public interface IEntitySampler extends ISampler<ADemoEntity> {

	public void setSample(Collection<ADemoEntity> sample);

	public void addObjectives(AFullNDimensionalMatrix<Integer> objectives);
	
	public Set<ADemoEntity> drawUnique(int numberOfDraw);
	
}
