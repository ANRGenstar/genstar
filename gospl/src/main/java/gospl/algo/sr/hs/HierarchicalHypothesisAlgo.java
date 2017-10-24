package gospl.algo.sr.hs;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.metamodel.pop.attribute.DemographicAttribute;
import core.metamodel.pop.io.GSSurveyType;
import core.metamodel.value.IValue;
import core.util.GSPerformanceUtil;
import gospl.algo.sr.ISyntheticReconstructionAlgo;
import gospl.algo.sr.hs.graph.AttributesDependanciesGraph;
import gospl.distribution.exception.IllegalDistributionCreation;
import gospl.distribution.matrix.ASegmentedNDimensionalMatrix;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.sampler.IHierarchicalSampler;
import gospl.sampler.ISampler;


/**
 * an inference algorithm that assesses the relationships between the attributes passed 
 * as inputs and defines an order for their usage. 
 * 
 * We assume we can always 
 * 
 * @author Kevin Chapuis
 * @author Samuel Thiriot
 *
 */
public class HierarchicalHypothesisAlgo implements ISyntheticReconstructionAlgo<IHierarchicalSampler> {

	private Logger logger = LogManager.getLogger();
	

	public HierarchicalHypothesisAlgo() {
	}
	


	@Override
	public ISampler<ACoordinate<DemographicAttribute<? extends IValue>, IValue>> inferSRSampler(
			INDimensionalMatrix<DemographicAttribute<? extends IValue>, IValue, Double> matrix, 
			IHierarchicalSampler sampler)
					throws IllegalDistributionCreation {
		
		// Begin the algorithm (and performance utility)
		GSPerformanceUtil gspu = new GSPerformanceUtil("Compute hierachical sampler from conditional distribution", logger);
		gspu.getStempPerformance(0);
		
		if (matrix == null)
			throw new IllegalArgumentException("matrix passed in parameter cannot be null or empty");
		
		if (!matrix.isSegmented() && matrix.getMetaDataType().equals(GSSurveyType.LocalFrequencyTable))
		 	throw new IllegalDistributionCreation("can't create a sampler using only one matrix of GosplMetaDataType#LocalFrequencyTable");
		
		// Stop the algorithm and exit the unique matrix if there is only one
		if(!matrix.isSegmented()){
			// TODO is that relevant ? 
			// TODO what to do ? sampler.setDistribution((AFullNDimensionalMatrix<Double>) matrix);
			throw new UnsupportedOperationException();
			//return sampler;
		}

		/////////////////////////////////////
		// 1st STEP: identify the various inner matrices
		/////////////////////////////////////

		// Cast matrix to access inner full matrices
		ASegmentedNDimensionalMatrix<Double> segmentedMatrix = (ASegmentedNDimensionalMatrix<Double>) matrix;


		// TODO to be defined
		
		// at this stage:
		// - allocatedAttributes contains all the attributes of interest here. 
		// - sampleDistribution contains the normalized frequencies (not conditional but local)
		// - segmentedMatrix corresponds to the data received as input segmentedMatrix.getMatrices() gives all the matrices
		
		logger.debug("end of process");
		
		for (INDimensionalMatrix<DemographicAttribute<? extends IValue>, IValue, Double> currentMatrix: segmentedMatrix.getMatrices()) {
			for (DemographicAttribute<? extends IValue> att: currentMatrix.getDimensions()) {
				logger.debug("	att: {}", att);
			}
		}

		AttributesDependanciesGraph dependancyGraph = AttributesDependanciesGraph.constructDependancies(segmentedMatrix);
		
		//File dotFile = dependancyGraph.printDotRepresentationToFile();
		//logger.info("a dot file was stored into "+dotFile.getAbsolutePath());
		
		// Use this to visualize the dependancy graph in png 
		//dependancyGraph.generateDotRepresentationInPNG(true);
		
		// TODO sampler.setDistribution(new GosplBasicDistribution(sampleDistribution));
		Collection<List<DemographicAttribute<? extends IValue>>> explorationOrder = proposeExplorationOrder(dependancyGraph);
		sampler.setDistribution(explorationOrder, segmentedMatrix);
		
		return sampler;
	}

	/**
	 * Based on the graph of dependancies, defines for each subgraph a valid order of usage of the attributes
	 * for the hierarchical sampling.
	 * @return
	 */
	public Collection<List<DemographicAttribute<? extends IValue>>> proposeExplorationOrder(AttributesDependanciesGraph dependancyGraph) {
		
		// first detect the subgraphs
		Collection<Set<DemographicAttribute<? extends IValue>>> independantGraphs = dependancyGraph.getConnectedComponents();
		
		Collection<List<DemographicAttribute<? extends IValue>>> res = new LinkedList<>();

		
		for (Set<DemographicAttribute<? extends IValue>> component: independantGraphs) {
		
			logger.debug("component {} ", component);
			
			// detect the candidate roots here 
			Set<DemographicAttribute<? extends IValue>> potentialRoots = dependancyGraph.getRoots(component);
			logger.debug("might start with roots: {}", potentialRoots);
			
			// TODO what is the more relevant ? Start smartly with the less low probabilities to reduce biasing ? 
			// with the highest or lowest cards ? 
			
			// well, right now we just select the first one :-/
			DemographicAttribute<? extends IValue> root = potentialRoots.iterator().next();
			
			// add now build the list !
			List<DemographicAttribute<? extends IValue>> orderForSubgraph = dependancyGraph.getOrderOfExploration(component, root);
			logger.debug("this component should be explore in this order: {}", orderForSubgraph);
			res.add(orderForSubgraph);
		}
		
		return res;
	}


}
