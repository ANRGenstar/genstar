package gospl.sampler.co;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import core.metamodel.IPopulation;
import core.metamodel.attribute.Attribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.value.IValue;
import core.util.random.GenstarRandomUtils;
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
		final List<ADemoEntity> buffer = new ArrayList<>(sample);
		return IntStream.range(0, numberOfDraw)
				.mapToObj(i -> _doClone(buffer, draw()))
				.collect(Collectors.toSet());
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
	
	private ADemoEntity _doClone(List<ADemoEntity> buffer, ADemoEntity entity) {
		return buffer.remove(entity) ? entity : entity.clone();
	}
	
}
