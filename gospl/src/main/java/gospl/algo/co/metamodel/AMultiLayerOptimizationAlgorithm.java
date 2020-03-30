package gospl.algo.co.metamodel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import core.metamodel.IPopulation;
import core.metamodel.attribute.Attribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.value.IValue;
import gospl.algo.co.metamodel.neighbor.IPopulationNeighborSearch;
import gospl.distribution.matrix.INDimensionalMatrix;

public abstract class AMultiLayerOptimizationAlgorithm extends AOptimizationAlgorithm {

	final private Map<Integer, Set<INDimensionalMatrix<Attribute<? extends IValue>, IValue, Integer>>> layeredObjectives;
	
	private Map<Integer, IPopulation<ADemoEntity, Attribute<? extends IValue>>> samples;
	private Map<Integer, IPopulationNeighborSearch<?>> layeredNeighborSearch;
	
	public AMultiLayerOptimizationAlgorithm(IPopulationNeighborSearch<?> neighborSearch, double fitnessThreshold) {
		super(neighborSearch, fitnessThreshold);
		this.layeredObjectives = new HashMap<>();
		this.layeredObjectives.put(0, new HashSet<>());
	}

	public void addObjectives(int layer, INDimensionalMatrix<Attribute<? extends IValue>, IValue, Integer> objectives) {
		super.addObjectives(objectives);
		this.layeredObjectives.get(0).add(objectives);
	}
	

	public IPopulation<ADemoEntity, Attribute<? extends IValue>> getSample(int layer){
		return this.samples.get(layer);
	}
	
	public void setSample(int layer, IPopulation<ADemoEntity, Attribute<? extends IValue>> sample) {
		this.samples.put(layer, sample);
		if(layeredNeighborSearch.containsKey(layer)) { this.layeredNeighborSearch.get(layer).setSample(sample); }
	}
	
	public IPopulationNeighborSearch<?> getNeighborSearchAlgorithm(int layer){
		return this.layeredNeighborSearch.get(layer);
	}
	
	public void setNeighborSearch(int layer, IPopulationNeighborSearch<?> neighborSearch) {
		this.layeredNeighborSearch.put(layer, neighborSearch);
	}

}
