package gospl.algo.ipf;

import core.metamodel.IPopulation;
import core.metamodel.pop.ADemoEntity;
import core.metamodel.pop.attribute.DemographicAttribute;
import core.metamodel.value.IValue;
import gospl.algo.sr.ISyntheticReconstructionAlgo;
import gospl.distribution.GosplNDimensionalMatrixFactory;
import gospl.distribution.exception.IllegalDistributionCreation;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.sampler.IDistributionSampler;
import gospl.sampler.ISampler;

public class SRIPFAlgo extends AGosplIPF<Double> implements ISyntheticReconstructionAlgo<IDistributionSampler> {

	public SRIPFAlgo(IPopulation<ADemoEntity, DemographicAttribute<? extends IValue>> seed) {
		super(seed);
	}
	
	public SRIPFAlgo(IPopulation<ADemoEntity, DemographicAttribute<? extends IValue>> seed,
			int step, double delta) {
		super(seed, step, delta);
	}

	@Override
	public ISampler<ACoordinate<DemographicAttribute<? extends IValue>, IValue>> inferSRSampler(
			INDimensionalMatrix<DemographicAttribute<? extends IValue>, IValue, Double> matrix, 
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
		return process(new GosplNDimensionalMatrixFactory().createDistribution(sampleSeed));
	}

}
