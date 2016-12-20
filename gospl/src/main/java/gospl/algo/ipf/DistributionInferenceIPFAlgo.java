package gospl.algo.ipf;

import core.metamodel.IPopulation;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;
import gospl.algo.ISyntheticReconstructionAlgo;
import gospl.algo.sampler.IDistributionSampler;
import gospl.algo.sampler.ISampler;
import gospl.distribution.GosplDistributionFactory;
import gospl.distribution.exception.IllegalDistributionCreation;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.distribution.matrix.coordinate.ACoordinate;

public class DistributionInferenceIPFAlgo extends AGosplIPF<Double> implements ISyntheticReconstructionAlgo<IDistributionSampler> {

	public DistributionInferenceIPFAlgo(IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> seed) {
		super(seed);
	}
	
	public DistributionInferenceIPFAlgo(IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> seed,
			int step, double delta) {
		super(seed, step, delta);
	}

	@Override
	public ISampler<ACoordinate<APopulationAttribute, APopulationValue>> inferSRSampler(
			INDimensionalMatrix<APopulationAttribute, APopulationValue, Double> matrix, 
			IDistributionSampler sampler)
			throws IllegalDistributionCreation {
		
		super.setMarginalMatrix(matrix);
		sampler.setDistribution(process());
		
		return sampler;
	}

	@Override
	public AFullNDimensionalMatrix<Double> process() {
		if(this.marginals == null || this.marginals.getMatrix().isEmpty()) 
			throw new IllegalArgumentException(this.getClass().getSimpleName()+" must define a matrix to setup marginals");	
		return process(GosplDistributionFactory.createDistribution(sampleSeed));
	}

}
