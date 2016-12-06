package gospl.algo.ipf;

import java.util.Collection;
import java.util.stream.Collectors;

import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;
import core.metamodel.pop.io.GSSurveyType;
import gospl.algo.IDistributionInferenceAlgo;
import gospl.algo.sampler.IDistributionSampler;
import gospl.algo.sampler.ISampler;
import gospl.distribution.GosplJointDistribution;
import gospl.distribution.exception.IllegalDistributionCreation;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.distribution.matrix.coordinate.ACoordinate;

public class DistributionInferenceIPFAlgo extends GosplIPF<Double> implements IDistributionInferenceAlgo<IDistributionSampler> {
	
	public DistributionInferenceIPFAlgo(Collection<APopulationEntity> seed) {
		super(seed);
	}

	@Override
	public ISampler<ACoordinate<APopulationAttribute, APopulationValue>> inferDistributionSampler(
			INDimensionalMatrix<APopulationAttribute, APopulationValue, Double> matrix, 
			IDistributionSampler sampler)
			throws IllegalDistributionCreation {
		
		super.setMarginalMatrix(matrix);
		sampler.setDistribution(super.process());
		
		return sampler;
	}

	@Override
	public AFullNDimensionalMatrix<Double> process(double convergenceDelta, int step) {
		AFullNDimensionalMatrix<Double> distribution = new GosplJointDistribution(
				this.matrix.getDimensions().stream().collect(Collectors.toMap(dim -> dim, dim -> dim.getValues())),
				GSSurveyType.GlobalFrequencyTable);
		// TODO Auto-generated method stub
		return null;
	}

}
