package gospl.algo.sampler.co;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
		return sample.stream().skip(GenstarRandom.getInstance()
				.nextInt(sample.size())).findFirst().get();
	}

	@Override
	public List<APopulationEntity> draw(int numberOfDraw) {
		return IntStream.range(0, numberOfDraw).mapToObj(i -> draw())
				.collect(Collectors.toList());
	}
	
	@Override
	public Set<APopulationEntity> drawUnique(int numberOfDraw){
		final List<APopulationEntity> tmpSample = new ArrayList<>(sample);
		Set<APopulationEntity> draws = new HashSet<>();
		for(int i = 0; i < numberOfDraw; i++){
			APopulationEntity newEntity = this.draw();
			draws.add(tmpSample.remove(newEntity) ? 
					newEntity : newEntity.clone());
		}
		return draws;
	}

	@Override
	public void setSample(Collection<APopulationEntity> sample) {
		this.sample = sample;
	}
	
	@Override
	public void addObjectives(AFullNDimensionalMatrix<Integer> objectives) {
		throw new IllegalAccessError("There is not any objectives to setup in RandomSampler");
	}
	
	@Override
	public String toCsv(String csvSeparator) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
