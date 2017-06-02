package gospl.algo.sampler.co;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import core.metamodel.pop.APopulationEntity;
import core.util.random.GenstarRandom;
import gospl.algo.sampler.IEntitySampler;
import gospl.distribution.matrix.AFullNDimensionalMatrix;

public class RandomSampler implements IEntitySampler {

	Collection<APopulationEntity> sample;
	
	@Override
	public APopulationEntity draw() {
		return (APopulationEntity) sample.toArray()[GenstarRandom.getInstance().nextInt(sample.size())];
	}

	@Override
	public List<APopulationEntity> draw(int numberOfDraw) {
		return IntStream.range(0, numberOfDraw).parallel().mapToObj(i -> draw()).collect(Collectors.toList());
	}

	@Override
	public void setSample(Collection<APopulationEntity> sample) {
		this.sample = sample;
	}

	@Override
	public void setObjectives(AFullNDimensionalMatrix<Integer> process) {
		throw new IllegalAccessError();
	}
	
	@Override
	public String toCsv(String csvSeparator) {
		// TODO Auto-generated method stub
		return null;
	}

}
