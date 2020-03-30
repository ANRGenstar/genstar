package gospl.sampler.multilayer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import core.metamodel.IPopulation;
import core.metamodel.attribute.Attribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.value.IValue;
import gospl.algo.co.metamodel.IOptimizationAlgorithm;
import gospl.algo.co.metamodel.solution.SyntheticPopulationAggregatedSolution;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.sampler.ISampler;

public class GosplBiLayerOptimizationSampler<A extends IOptimizationAlgorithm> implements ICOMultiLayerSampler {

	private IPopulation<ADemoEntity, Attribute<? extends IValue>> multiLayerSample;
	
	final private Map<Integer, IPopulation<ADemoEntity, Attribute<? extends IValue>>> samples;
	final private Map<Integer, INDimensionalMatrix<Attribute<? extends IValue>, IValue, Integer>> objectives;
	
	private ISampler<ADemoEntity> basicSampler;
	
	private final A algorithm;
	
	public GosplBiLayerOptimizationSampler(A algorithm) {
		this.algorithm = algorithm;
		this.samples = new HashMap<>();
		this.objectives = new HashMap<>();
	}
	
	@Override
	public void setSample(IPopulation<ADemoEntity, Attribute<? extends IValue>> sample) {
		this.multiLayerSample = sample;
		this.algorithm.setSample(sample);
	}
	
	@Override
	public void setSample(IPopulation<ADemoEntity, Attribute<? extends IValue>> sample, int layer) {
		this.samples.put(layer, sample);
	}

	@Override
	public void addObjectives(INDimensionalMatrix<Attribute<? extends IValue>, IValue, Integer> objectives) {
		this.objectives.put(0,objectives);
	}
	
	@Override
	public void addObjectives(INDimensionalMatrix<Attribute<? extends IValue>, IValue, Integer> objectives, int layer) {
		this.objectives.put(layer, objectives);
	}

	@Override
	public ADemoEntity draw() {
		return basicSampler.draw();
	}
	
	@Override
	public Collection<ADemoEntity> draw(int numberOfDraw) {
		return this.algorithm.run(
					new SyntheticPopulationAggregatedSolution(this.basicSampler.draw(numberOfDraw)))
			.getSolution();
	}

	@Override
	public String toCsv(String csvSeparator) {
		// TODO Auto-generated method stub
		return null;
	}

}
