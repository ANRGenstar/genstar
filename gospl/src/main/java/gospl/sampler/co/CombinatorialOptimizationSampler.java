package gospl.sampler.co;

import java.util.Collection;

import core.metamodel.IPopulation;
import core.metamodel.attribute.Attribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.value.IValue;
import gospl.algo.co.metamodel.IOptimizationAlgorithm;
import gospl.algo.co.metamodel.solution.SyntheticPopulationSolution;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.sampler.IEntitySampler;

/**
 * Define higher order behavior for {@link IEntitySampler}. It relies on {@link UniformSampler}
 * 
 * @author kevinchapuis
 *
 * @param <A>
 */
public class CombinatorialOptimizationSampler<A extends IOptimizationAlgorithm> implements IEntitySampler {

	private UniformSampler basicSampler;
	private A algorithm;

	protected boolean dataBasedPopulation;

	public CombinatorialOptimizationSampler(A algorithm, 
			IPopulation<ADemoEntity, Attribute<? extends IValue>> sample,
			boolean dataBasedPopulation) {
		this.algorithm = algorithm;
		this.basicSampler = new UniformSampler();
		this.setSample(sample);
		this.dataBasedPopulation = dataBasedPopulation;
	}
	
	@Override
	public ADemoEntity draw() {
		return basicSampler.draw();
	}
	
	@Override
	public Collection<ADemoEntity> draw(int numberOfDraw) {
		return this.algorithm.run(new SyntheticPopulationSolution(
				this.basicSampler.draw(numberOfDraw), dataBasedPopulation)).getSolution();
	}
	
	@Override
	public void setSample(IPopulation<ADemoEntity, Attribute<? extends IValue>> sample) {
		this.basicSampler.setSample(sample);
		this.algorithm.setSample(sample);
	}

	@Override
	public void addObjectives(INDimensionalMatrix<Attribute<? extends IValue>, IValue, Integer> objectives) {
		this.algorithm.addObjectives(objectives);
	}
	
	@Override
	public String toCsv(String csvSeparator) {
		// TODO Auto-generated method stub
		return null;
	}

}
