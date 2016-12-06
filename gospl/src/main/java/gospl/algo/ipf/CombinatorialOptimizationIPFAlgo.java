package gospl.algo.ipf;

import java.util.Collection;

import core.metamodel.IPopulation;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;
import gospl.algo.ICombinatorialOptimizationAlgo;
import gospl.algo.sampler.IEntitySampler;
import gospl.algo.sampler.ISampler;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.INDimensionalMatrix;

public class CombinatorialOptimizationIPFAlgo extends GosplIPF<Integer> implements ICombinatorialOptimizationAlgo<IEntitySampler> {

	public CombinatorialOptimizationIPFAlgo(Collection<APopulationEntity> seed,
			INDimensionalMatrix<APopulationAttribute, APopulationValue, Integer> matrix) {
		super(seed);
		super.setMarginalMatrix(matrix);
	}
	
	@Override
	public ISampler<APopulationEntity> inferCOSampler(
			IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> sample, 
			IEntitySampler sampler) {
		
		// TODO: test if sample fits to ipf controls
		
		sampler.setSample(sample);
		sampler.setObjectives(super.process());
		
		return sampler;
	}

	@Override
	public AFullNDimensionalMatrix<Integer> process(double convergenceDelta, int step) {
		// TODO Auto-generated method stub
		return null;
	}

}
