package gospl.algo.hs;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationValue;
import core.metamodel.pop.io.GSSurveyType;
import core.util.GSPerformanceUtil;
import gospl.algo.ISyntheticReconstructionAlgo;
import gospl.algo.hs.graph.AttributesDependanciesGraph;
import gospl.algo.sampler.IHierarchicalSampler;
import gospl.algo.sampler.ISampler;
import gospl.distribution.exception.IllegalDistributionCreation;
import gospl.distribution.matrix.ASegmentedNDimensionalMatrix;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.distribution.matrix.coordinate.ACoordinate;


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
	public ISampler<ACoordinate<APopulationAttribute, APopulationValue>> inferSRSampler(
			INDimensionalMatrix<APopulationAttribute, APopulationValue, Double> matrix, 
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
		
		for (INDimensionalMatrix<APopulationAttribute, APopulationValue, Double> currentMatrix: segmentedMatrix.getMatrices()) {
			for (APopulationAttribute att: currentMatrix.getDimensions()) {
				logger.debug("	att: {}", att);
			}
		}

		AttributesDependanciesGraph dependancyGraph = AttributesDependanciesGraph.constructDependancies(segmentedMatrix);
		
		//File dotFile = dependancyGraph.printDotRepresentationToFile();
		//logger.info("a dot file was stored into "+dotFile.getAbsolutePath());
		
		// Use this to visualize the dependancy graph in png 
		//dependancyGraph.generateDotRepresentationInPNG(true);
		
		// TODO sampler.setDistribution(new GosplBasicDistribution(sampleDistribution));
		Collection<List<APopulationAttribute>> explorationOrder = proposeExplorationOrder(dependancyGraph);
		sampler.setDistribution(explorationOrder, segmentedMatrix);
		
		return sampler;
	}

	/**
	 * Based on the graph of dependancies, defines for each subgraph a valid order of usage of the attributes
	 * for the hierarchical sampling.
	 * @return
	 */
	public Collection<List<APopulationAttribute>> proposeExplorationOrder(AttributesDependanciesGraph dependancyGraph) {
		
		// first detect the subgraphs
		Collection<Set<APopulationAttribute>> independantGraphs = dependancyGraph.getConnectedComponents();
		
		Collection<List<APopulationAttribute>> res = new LinkedList<>();

		
		for (Set<APopulationAttribute> component: independantGraphs) {
		
			logger.debug("component {} ", component);
			
			// detect the candidate roots here 
			Set<APopulationAttribute> potentialRoots = dependancyGraph.getRoots(component);
			logger.debug("might start with roots: {}", potentialRoots);
			
			// TODO what is the more relevant ? Start smartly with the less low probabilities to reduce biasing ? 
			// with the highest or lowest cards ? 
			
			// well, right now we just select the first one :-/
			APopulationAttribute root = potentialRoots.iterator().next();
			
			// add now build the list !
			List<APopulationAttribute> orderForSubgraph = dependancyGraph.getOrderOfExploration(component, root);
			logger.debug("this component should be explore in this order: {}", orderForSubgraph);
			res.add(orderForSubgraph);
		}
		
		return res;
	}


}
