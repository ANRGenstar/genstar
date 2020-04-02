package gospl.sampler.co;

import java.util.Collection;

import core.metamodel.IPopulation;
import core.metamodel.attribute.Attribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.value.IValue;
import gospl.algo.co.metamodel.IOptimizationAlgorithm;
import gospl.algo.co.metamodel.solution.SyntheticPopulationAggregatedSolution;
import gospl.algo.co.metamodel.solution.SyntheticPopulationSolution;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.sampler.IEntitySampler;

/**
 * Define higher order behavior for {@link IEntitySampler}. It relies on {@link MicroDataSampler}
 * 
 * @author kevinchapuis
 *
 * @param <A>
 */
public class CombinatorialOptimizationSampler<A extends IOptimizationAlgorithm> implements IEntitySampler {

	private MicroDataSampler basicSampler;
	private A algorithm;

	protected boolean dataBasedPopulation;
	protected boolean aggregatedMarginals;

	public CombinatorialOptimizationSampler(A algorithm, 
			IPopulation<ADemoEntity, Attribute<? extends IValue>> sample) {
		this(algorithm, sample, false, false);
	}
	
	public CombinatorialOptimizationSampler(A algorithm, 
			IPopulation<ADemoEntity, Attribute<? extends IValue>> sample,
			boolean dataBasedPopulation, boolean aggregatedMarginals) {
		this.algorithm = algorithm;
		this.basicSampler = new MicroDataSampler();
		this.setSample(sample,false);
		this.dataBasedPopulation = dataBasedPopulation;
		this.aggregatedMarginals = aggregatedMarginals;
	}
	
	@Override
	public ADemoEntity draw() {
		return basicSampler.draw();
	}
	
	@Override
	public Collection<ADemoEntity> draw(int numberOfDraw) {
		return this.algorithm.run(
				aggregatedMarginals ?
					new SyntheticPopulationAggregatedSolution(this.basicSampler.draw(numberOfDraw)) :
					new SyntheticPopulationSolution(this.basicSampler.draw(numberOfDraw), dataBasedPopulation))
			.getSolution();
	}
	
	@Override
	public void setSample(IPopulation<ADemoEntity, Attribute<? extends IValue>> sample, boolean weights) {
		this.basicSampler.setSample(sample,weights);
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
