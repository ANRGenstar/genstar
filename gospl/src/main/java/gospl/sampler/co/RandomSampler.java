package gospl.sampler.co;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import core.metamodel.pop.ADemoEntity;
import core.util.random.GenstarRandom;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.sampler.IEntitySampler;

public class RandomSampler implements IEntitySampler {

	Collection<ADemoEntity> sample;
	
	@Override
	public ADemoEntity draw() {
		return sample.stream().skip(GenstarRandom.getInstance()
				.nextInt(sample.size())).findFirst().get();
	}

	@Override
	public List<ADemoEntity> draw(int numberOfDraw) {
		return IntStream.range(0, numberOfDraw).mapToObj(i -> draw())
				.collect(Collectors.toList());
	}
	
	@Override
	public Set<ADemoEntity> drawUnique(int numberOfDraw){
		final List<ADemoEntity> tmpSample = new ArrayList<>(sample);
		Set<ADemoEntity> draws = new HashSet<>();
		for(int i = 0; i < numberOfDraw; i++){
			ADemoEntity newEntity = this.draw();
			draws.add(tmpSample.remove(newEntity) ? 
					newEntity : (ADemoEntity) newEntity.clone());
		}
		return draws;
	}

	@Override
	public void setSample(Collection<ADemoEntity> sample) {
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
