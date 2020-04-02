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
import gospl.algo.co.metamodel.solution.ISyntheticPopulationSolution;
import gospl.algo.co.metamodel.solution.MultiLayerSPSolution;
import gospl.distribution.matrix.INDimensionalMatrix;

public abstract class AMultiLayerOptimizationAlgorithm implements IOptimizationAlgorithm<MultiLayerSPSolution> {

	/*
	 * Constraint on searching for an optimal solution
	 */
	final private Map<Integer, Set<INDimensionalMatrix<Attribute<? extends IValue>, IValue, Integer>>> layeredObjectives;
	private final Map<Integer,Integer> layerSizeConstraint;
	
	/*
	 * Should be used to draw individual according to the level of population
	 */
	private Map<Integer, IPopulation<ADemoEntity, Attribute<? extends IValue>>> samples;
	private Map<Integer, IPopulationNeighborSearch<?>> layeredNeighborSearch;

	private IPopulation<ADemoEntity, Attribute<? extends IValue>> sample;
	private IPopulationNeighborSearch<?> neighborSearch;
	private int layer;

	private double fitnessThreshold;
	private double k_neighborRatio;
	
	public AMultiLayerOptimizationAlgorithm(IPopulationNeighborSearch<?> neighborSearch, double fitnessThreshold) {
		this.neighborSearch = neighborSearch; 
		this.fitnessThreshold = fitnessThreshold;
		this.samples = new HashMap<>();
		this.layeredObjectives = new HashMap<>();
		this.layeredNeighborSearch = new HashMap<>();
		this.layerSizeConstraint = new HashMap<>();
	}
	
	public IPopulationNeighborSearch<?> getNeighborSearchAlgorithm(int layer){
		return this.layeredNeighborSearch.get(layer);
	}
	
	public void setNeighborSearch(int layer, IPopulationNeighborSearch<?> neighborSearch) {
		this.layeredNeighborSearch.put(layer, neighborSearch);
	}
	
	@Override
	public Set<INDimensionalMatrix<Attribute<? extends IValue>, IValue, Integer>> getObjectives() {
		return layeredObjectives.get(0);
	}
	
	public Map<Integer, Set<INDimensionalMatrix<Attribute<? extends IValue>, IValue, Integer>>> getLayeredObjectives() {
		return layeredObjectives;
	}

	@Override
	public void addObjectives(INDimensionalMatrix<Attribute<? extends IValue>, IValue, Integer> objectives) {
		this.addObjectives(0, objectives);
	}
	
	public void addObjectives(int layer, INDimensionalMatrix<Attribute<? extends IValue>, IValue, Integer> objectives) {
		if(layeredObjectives.containsKey(layer))
			this.layeredObjectives.get(layer).add(objectives);
		else {
			this.layeredObjectives.put(layer, new HashSet<>());
			this.layeredObjectives.get(layer).add(objectives);
		}
		this.layerSizeConstraint.put(layer, objectives.getVal().getValue());
	}
	
	@Override
	public IPopulation<ADemoEntity, Attribute<? extends IValue>> getSample(){
		return sample;
	}
	
	public IPopulation<ADemoEntity, Attribute<? extends IValue>> getSample(int layer){
		return this.samples.get(layer);
	}
	
	@Override
	public void setSample(IPopulation<ADemoEntity, Attribute<? extends IValue>> sample) {
		this.setSample(0, sample);
	}
	
	/**
	 * Define a sample and the level to draw from : e.g. could be -1 if we want to draw child of this population entities
	 * @param layer
	 * @param sample
	 */
	public void setSample(int layer, IPopulation<ADemoEntity, Attribute<? extends IValue>> sample) {
		this.sample = sample;
		this.layer = layer;
		if (layer==0) { neighborSearch.setSample(sample); }
		else if(layeredNeighborSearch.containsKey(layer)) { this.layeredNeighborSearch.get(layer).setSample(sample); }
	}
	
	@Override
	public IPopulationNeighborSearch<?> getNeighborSearchAlgorithm(){
		return neighborSearch;
	}
	
	@Override
	public void setNeighborSearch(IPopulationNeighborSearch<?> neighborSearch) {
		this.neighborSearch = neighborSearch;
	}
	
	public void addNeighborSearch(int layer, IPopulationNeighborSearch<?> neighborSearch) {
		this.layeredNeighborSearch.put(layer, neighborSearch);
	}

	@Override
	public double getFitnessThreshold() {
		return fitnessThreshold;
	}

	@Override
	public void setFitnessThreshold(double fitnessThreshold) {
		this.fitnessThreshold = fitnessThreshold;
	}

	@Override
	public double getK_neighborRatio() {
		return k_neighborRatio;
	}

	@Override
	public void setK_neighborRatio(double k_neighborRatio) {
		this.k_neighborRatio = k_neighborRatio;
	}
	
	public int getSampledLayer() {return layer;}
	
	public void setSampledLayer(int layer) {this.layer = layer;}
	
	public int computeBuffer(double fitness, ISyntheticPopulationSolution solution) {
		return Math.round(Math.round(solution.getSolution().size() * k_neighborRatio * Math.log(1+fitness*k_neighborRatio)));
	}

}
