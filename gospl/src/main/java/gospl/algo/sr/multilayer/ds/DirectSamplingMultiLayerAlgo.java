package gospl.algo.sr.multilayer.ds;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.metamodel.attribute.Attribute;
import core.metamodel.io.GSSurveyType;
import core.metamodel.value.IValue;
import core.util.GSPerformanceUtil;
import gospl.algo.sr.multilayer.ISynthethicReconstructionMultiLayerAlgo;
import gospl.distribution.exception.IllegalDistributionCreation;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.sampler.ISampler;
import gospl.sampler.multilayer.IMultiLayerSampler;

public class DirectSamplingMultiLayerAlgo<SamplerType extends IMultiLayerSampler> implements ISynthethicReconstructionMultiLayerAlgo<SamplerType> {

	private Logger logger = LogManager.getLogger();
	
	@Override
	public ISampler<ACoordinate<Attribute<? extends IValue>, IValue>> inferSRMLSampler(
			INDimensionalMatrix<Attribute<? extends IValue>, IValue, Double> topMatrix, 
			INDimensionalMatrix<Attribute<? extends IValue>, IValue, Double> bottomMatrix,
			SamplerType sampler)
			throws IllegalDistributionCreation {
		
		// Begin the algorithm (and performance utility)
		GSPerformanceUtil gspu = new GSPerformanceUtil("Compute hierachical sampler from conditional distribution", logger);
		gspu.getStempPerformance(0);
		

		if (topMatrix == null || bottomMatrix == null)
			throw new IllegalArgumentException("Matrix passed in parameter cannot be null or empty");
		
		if ((!topMatrix.isSegmented() && topMatrix.getMetaDataType().equals(GSSurveyType.LocalFrequencyTable))
				|| !bottomMatrix.isSegmented() && bottomMatrix.getMetaDataType().equals(GSSurveyType.LocalFrequencyTable))
		 	throw new IllegalDistributionCreation("Can't create a sampler using GosplMetaDataType#LocalFrequencyTable");
		
		// TODO: draw the first layer according to topMatrix
		
		
		
		// TODO: draw the second layer
		
		return null;
	}


}
