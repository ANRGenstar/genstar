package gospl.algo.co.metamodel.solution;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import core.metamodel.IPopulation;
import core.metamodel.attribute.Attribute;
import core.metamodel.attribute.IAttribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.entity.IEntity;
import core.metamodel.value.IValue;
import gospl.algo.co.metamodel.neighbor.IPopulationNeighborSearch;
import gospl.distribution.GosplNDimensionalMatrixFactory;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.validation.GosplIndicatorFactory;

/**
 * A Synthetic population that provides a ready to access exploration feature in term of fitness and neighbors 
 * 
 * @author kevinchapuis
 *
 */
public class MultiLayerSPSolution implements ISyntheticPopulationSolution {

	final private SyntheticPopulationSolution sps;
	final private Map<Integer, Double> layeredFitness;
	
	public MultiLayerSPSolution(IPopulation<ADemoEntity, Attribute<? extends IValue>> population, boolean dataBasedPopulation) {
		this.sps = new SyntheticPopulationSolution(population, dataBasedPopulation);
		this.layeredFitness = new HashMap<>();
	}
	
	@Override
	public <U> Collection<ISyntheticPopulationSolution> getNeighbors(IPopulationNeighborSearch<U> neighborSearch) {
		return sps.getNeighbors(neighborSearch);
	}

	@Override
	public <U> Collection<ISyntheticPopulationSolution> getNeighbors(IPopulationNeighborSearch<U> neighborSearch,
			int k_neighbors) {
		return sps.getNeighbors(neighborSearch, k_neighbors);
	}

	@Override
	public <U> ISyntheticPopulationSolution getRandomNeighbor(IPopulationNeighborSearch<U> neighborSearch) {
		return sps.getRandomNeighbor(neighborSearch);
	}

	@Override
	public <U> ISyntheticPopulationSolution getRandomNeighbor(IPopulationNeighborSearch<U> neighborSearch,
			int k_neighbors) {
		return sps.getRandomNeighbor(neighborSearch);
	}

	@Override
	public Double getFitness(Set<INDimensionalMatrix<Attribute<? extends IValue>, IValue, Integer>> objectives) {
		return sps.getFitness(objectives);
	}
	
	public Double getFitness(int layer, Set<INDimensionalMatrix<Attribute<? extends IValue>, IValue, Integer>> objectives) {
		if(layeredFitness.containsKey(layer)){
			return layeredFitness.get(layer);
		} else {
			Collection<? extends IEntity<? extends IAttribute<? extends IValue>>> popLayer = sps.getSolution();
			for (int l = 0; l <= layer; l++) {
				popLayer = popLayer.stream().flatMap(entity -> entity.getChildren().stream()).collect(Collectors.toSet());
				if(popLayer.isEmpty()) { throw new IllegalArgumentException("There is no "+l+" layer in the current population"); }
			}
			
			AFullNDimensionalMatrix<Integer> popMatrix = GosplNDimensionalMatrixFactory
					.getFactory().createContingency(popLayer);
			double fitness = objectives.stream().mapToDouble(obj -> GosplIndicatorFactory.getFactory()
					.getIntegerTAE(obj, popMatrix)).sum();
			layeredFitness.put(layer, fitness);
			return fitness;
		}
	}

	@Override
	public IPopulation<ADemoEntity, Attribute<? extends IValue>> getSolution() {
		return sps.getSolution();
	}

}
