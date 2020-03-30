package gospl.sampler.multilayer;

import core.metamodel.IPopulation;
import core.metamodel.attribute.Attribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.value.IValue;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.sampler.IEntitySampler;

public interface ICOMultiLayerSampler extends IEntitySampler {

	public void setSample(IPopulation<ADemoEntity, Attribute<? extends IValue>> sample, int layer);

	public void addObjectives(INDimensionalMatrix<Attribute<? extends IValue>, IValue, Integer> objectives, int layer);
	
}
