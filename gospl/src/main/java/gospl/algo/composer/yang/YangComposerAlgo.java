package gospl.algo.composer.yang;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.metamodel.attribute.Attribute;
import core.metamodel.value.IValue;
import gospl.GosplPopulation;
import gospl.distribution.GosplContingencyTable;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.distribution.matrix.control.AControl;
import gospl.distribution.matrix.coordinate.ACoordinate;

public class YangComposerAlgo {

	private static Logger logger = LogManager.getLogger();

	protected GosplPopulation popParents;
	protected GosplPopulation popChildren;
	protected INDimensionalMatrix<Attribute<? extends IValue>, IValue, Double> pMatching;
	
	public YangComposerAlgo(
			GosplPopulation popParents,
			GosplPopulation popChildren,
			INDimensionalMatrix<Attribute<? extends IValue>, IValue, Double> pMatching
			) {
		
		this.popParents = popChildren;
		this.popChildren = popChildren;
		this.pMatching = pMatching;
	}
	
	
	/**
	 * based on the properties of the parents population,
	 * and on the matching probabilities,
	 * construct a matrix of how many children having each probabilities 
	 * are required
	 */
	public void computeExpectedChildrenProperties() {
		
		Set<Attribute<? extends IValue>> parentAttributes = new HashSet<>(popParents.getPopulationAttributes());
		parentAttributes.retainAll(pMatching.getDimensions());
		
		logger.info("computing for the dimensions: {}", parentAttributes);
		
		// 
		Map<Set<Attribute<? extends IValue>>,Integer> parents2matchingCandidates = new HashMap<>();
		for (Entry<ACoordinate<Attribute<? extends IValue>, IValue>, AControl<Double>> e: 
			pMatching.getMatrix().entrySet()) {
			
		}
		
		GosplContingencyTable c = new GosplContingencyTable(pMatching.getDimensions());
		// for each coordinate of the probabilities table 
		// 1) compute the 

		
	}

}
