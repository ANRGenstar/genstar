package gospl.algo.co.metamodel;

import java.util.HashSet;
import java.util.Set;

import core.metamodel.IPopulation;
import core.metamodel.attribute.Attribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.value.IValue;
import gospl.algo.co.metamodel.neighbor.IPopulationNeighborSearch;
import gospl.distribution.matrix.INDimensionalMatrix;

/**
 * Encapsulation of common properties for all CO algorithm: sample, neighboring search algorithm and objectives
 * <p>
 * Sample: the sample of a portion of the population to draw entity from <br/>
 * Neighbor search algorithm: the algorithm that will be responsible for neighbor population definition <br/>
 * Objectives: the objectives that will make possible fitness computation
 * @author kevinchapuis
 *
 */
public abstract class AOptimizationAlgorithm implements IOptimizationAlgorithm {

	private Set<INDimensionalMatrix<Attribute<? extends IValue>, IValue, Integer>> objectives;
	private IPopulationNeighborSearch<?> neighborSearch;
	private IPopulation<ADemoEntity, Attribute<? extends IValue>> sample;
	
	private double fitnessThreshold;
	private double k_neighborRatio = Math.pow(10, -3);
	
	/**
	 * CO Algorithm without sample to be set futher on
	 * @param neighborSearch
	 */
	public AOptimizationAlgorithm(IPopulationNeighborSearch<?> neighborSearch,
			double fitnessThreshold) {
		this.neighborSearch = neighborSearch;
		this.objectives = new HashSet<>();
		this.setFitnessThreshold(fitnessThreshold);
	}
	
	public AOptimizationAlgorithm(IPopulationNeighborSearch<?> neighborSearch,
			IPopulation<ADemoEntity, Attribute<? extends IValue>> sample,
			double fitnessThreshold) {
		this.neighborSearch = neighborSearch;
		this.setSample(sample);
		this.objectives = new HashSet<>();
		this.setFitnessThreshold(fitnessThreshold);
	}

	@Override
	public Set<INDimensionalMatrix<Attribute<? extends IValue>, IValue, Integer>> getObjectives() {
		return objectives;
	}

	@Override
	public void addObjectives(INDimensionalMatrix<Attribute<? extends IValue>, IValue, Integer> objectives) {
		this.objectives.add(objectives);
	}
	
	@Override
	public IPopulation<ADemoEntity, Attribute<? extends IValue>> getSample(){
		return this.sample;
	}
	
	@Override
	public void setSample(IPopulation<ADemoEntity, Attribute<? extends IValue>> sample) {
		this.sample = sample;
		this.neighborSearch.setSample(sample);
	}
	
	@Override
	public IPopulationNeighborSearch<?> getNeighborSearchAlgorithm(){
		return neighborSearch;
	}
	
	@Override
	public void setNeighborSearch(IPopulationNeighborSearch<?> neighborSearch) {
		this.neighborSearch = neighborSearch;
	}

	@Override
	public double getFitnessThreshold() {
		return fitnessThreshold;
	}

	@Override
	public void setFitnessThreshold(double fitnessThreshold) {
		this.fitnessThreshold = fitnessThreshold;
	}

	public double getK_neighborRatio() {
		return k_neighborRatio;
	}

	public void setK_neighborRatio(double k_neighborRatio) {
		this.k_neighborRatio = k_neighborRatio;
	}
	
}
