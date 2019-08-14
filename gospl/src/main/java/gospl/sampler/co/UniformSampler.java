package gospl.sampler.co;

import java.util.Collection;
import java.util.stream.IntStream;

import core.metamodel.IPopulation;
import core.metamodel.attribute.Attribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.value.IValue;
import core.util.random.GenstarRandomUtils;
import gospl.GosplPopulation;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.sampler.IEntitySampler;

public class UniformSampler implements IEntitySampler {

	Collection<ADemoEntity> sample;
	
	@Override
	public ADemoEntity draw() {
		return GenstarRandomUtils.oneOf(sample);
	}

	@Override
	public Collection<ADemoEntity> draw(int numberOfDraw) {
		GosplPopulation pop = new GosplPopulation();
		IntStream.range(0, numberOfDraw)
				.mapToObj(i -> draw().clone())
				.forEach(e -> pop.add(e));
		return pop;
	}

	@Override
	public void setSample(IPopulation<ADemoEntity, Attribute<? extends IValue>> sample) {
		this.sample = sample;
	}
	
	@Override
	public void addObjectives(INDimensionalMatrix<Attribute<? extends IValue>, IValue, Integer> objectives) {
		throw new IllegalAccessError("There is not any objectives to setup in RandomSampler");
	}
	
	@Override
	public String toCsv(String csvSeparator) {
		// TODO Auto-generated method stub
		return null;
	}
}
