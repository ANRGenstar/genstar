package gospl.algo.ipf;

import core.metamodel.IPopulation;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;
import gospl.algo.ICombinatorialOptimizationAlgo;
import gospl.algo.sampler.IEntitySampler;
import gospl.algo.sampler.ISampler;
import gospl.distribution.GosplNDimensionalMatrixFactory;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.INDimensionalMatrix;

public class CombinatorialOptimizationIPFAlgo extends AGosplIPF<Integer> implements ICombinatorialOptimizationAlgo<IEntitySampler> {

	public CombinatorialOptimizationIPFAlgo(IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> seed,
			INDimensionalMatrix<APopulationAttribute, APopulationValue, Integer> matrix) {
		super(seed);
		super.setMarginalMatrix(matrix);
	}
	
	public CombinatorialOptimizationIPFAlgo(IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> seed,
			INDimensionalMatrix<APopulationAttribute, APopulationValue, Integer> matrix,
			int step, double delta) {
		super(seed, step, delta);
		super.setMarginalMatrix(matrix);
	}
	
	@Override
	public ISampler<APopulationEntity> inferCOSampler(
			IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> sample, 
			IEntitySampler sampler) {
		
		sampler.setSample(sample);
		sampler.setObjectives(process());
		
		return sampler;
	}

	@Override
	public AFullNDimensionalMatrix<Integer> process() {
		if(this.marginals == null || this.marginals.getMatrix().isEmpty()) 
			throw new IllegalArgumentException(this.getClass().getSimpleName()+" must define a matrix to setup marginals");
		return process(new GosplNDimensionalMatrixFactory().createContingency(sampleSeed));
	}

}
