package gospl.sampler.co;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;

import core.metamodel.IPopulation;
import core.metamodel.attribute.Attribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.value.IValue;
import core.util.random.GenstarRandomUtils;
import core.util.random.roulette.ARouletteWheelSelection;
import core.util.random.roulette.RouletteWheelSelectionFactory;
import gospl.GosplPopulation;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.sampler.IEntitySampler;

/**
 * Draw an entity from a collection of entity
 * 
 * @author kevinchapuis
 *
 */
public class MicroDataSampler implements IEntitySampler {

	Collection<ADemoEntity> sample;
	
	ARouletteWheelSelection<Double, ADemoEntity> roulette;
	
	@Override
	public ADemoEntity draw() {
		return (roulette!=null ? (ADemoEntity) roulette.drawObject() : GenstarRandomUtils.oneOf(sample)).clone();
	}

	@Override
	public Collection<ADemoEntity> draw(int numberOfDraw) {
		GosplPopulation pop = new GosplPopulation();
		IntStream.range(0, numberOfDraw).mapToObj(i -> draw()).forEach(e -> pop.add(e));
		return pop;
	}
	
	public Collection<ADemoEntity> drawWithChildrenNumber(int numberOfDraw) {
		GosplPopulation pop = new GosplPopulation();
		do { pop.add(draw()); } while (pop.stream().flatMap(entity -> entity.getChildren().stream()).count() < numberOfDraw);
		return pop;
	}
	
	/**
	 * Add a sample with weights on individual
	 * @param sample
	 */
	@Override
	public void setSample(IPopulation<ADemoEntity, Attribute<? extends IValue>> sample, boolean weights) {
		if(weights) {
			List<Double> w = new ArrayList<>();
			List<ADemoEntity> e = new ArrayList<>();
			for (ADemoEntity entity : sample) { w.add(entity.getWeight()); e.add(entity); }
			this.roulette = RouletteWheelSelectionFactory.getRouletteWheel(w, e);
		} else {
			this.sample = sample;
		}
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
