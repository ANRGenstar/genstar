package gospl.algo.co.metamodel.solution;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import core.metamodel.IPopulation;
import core.metamodel.attribute.Attribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.value.IValue;
import core.util.random.GenstarRandomUtils;
import gospl.GosplPopulation;
import gospl.algo.co.metamodel.neighbor.IPopulationNeighborSearch;
import gospl.distribution.GosplNDimensionalMatrixFactory;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.validation.GosplIndicatorFactory;

public class SyntheticPopulationAggregatedSolution implements ISyntheticPopulationSolution {

	private IPopulation<ADemoEntity, Attribute<? extends IValue>> population;
	
	private AFullNDimensionalMatrix<Integer> marginals;
	
	private double fitness = -1;
	
	public SyntheticPopulationAggregatedSolution(IPopulation<ADemoEntity, Attribute<? extends IValue>> population,
			AFullNDimensionalMatrix<Integer> marginals){
		this.population = new GosplPopulation(population);
		this.marginals = marginals;
	}
	
	public SyntheticPopulationAggregatedSolution(Collection<ADemoEntity> population,
			AFullNDimensionalMatrix<Integer> marginals){
		this.population = new GosplPopulation(population);
		this.marginals = marginals;
	}
	
	// ----------------------- //
	
	@Override
	public <U> Collection<ISyntheticPopulationSolution> getNeighbors(IPopulationNeighborSearch<U> neighborSearch) {
		 return getNeighbors(neighborSearch, 1);
	}
	
	@Override
	public <U> Collection<ISyntheticPopulationSolution> getNeighbors(IPopulationNeighborSearch<U> neighborSearch,
			int k_neighbors) {
		
		Collection<ISyntheticPopulationSolution> neighbors = new ArrayList<>();
		
		for(U predicate : neighborSearch.getPredicates()) {
			Map<ADemoEntity, ADemoEntity> theSwitch = neighborSearch
				.getPairwisedEntities(this.population, predicate, k_neighbors);
			
			AFullNDimensionalMatrix<Integer> marginals = this.makeSwitch(
					new GosplNDimensionalMatrixFactory().createContingency(this.marginals), theSwitch); 

			neighbors.add(new SyntheticPopulationAggregatedSolution(
					neighborSearch.getNeighbor(population, theSwitch), marginals));
		}
		
		return neighbors;
	}
	
	@Override
	public <U> ISyntheticPopulationSolution getRandomNeighbor(IPopulationNeighborSearch<U> neighborSearch) {
		return getRandomNeighbor(neighborSearch, 1);
	}

	@Override
	public <U> ISyntheticPopulationSolution getRandomNeighbor(IPopulationNeighborSearch<U> neighborSearch, 
			int k_neighbors) {
		Map<ADemoEntity, ADemoEntity> theSwitch = neighborSearch.getPairwisedEntities(this.population, 
				GenstarRandomUtils.oneOf(neighborSearch.getPredicates()), k_neighbors);
		
		return new SyntheticPopulationAggregatedSolution(neighborSearch.getNeighbor(this.population, theSwitch), 
				makeSwitch(new GosplNDimensionalMatrixFactory().createContingency(this.marginals), theSwitch));
	}
	
	// ----------------------- //

	@Override
	public Double getFitness(Set<INDimensionalMatrix<Attribute<? extends IValue>, IValue, Integer>> objectives) {
		// Only compute once
		if(fitness == -1){
			fitness = objectives.stream().mapToDouble(obj -> GosplIndicatorFactory.getFactory()
					.getIntegerTAE(obj, marginals)).sum();
		}
		return fitness;
	}

	@Override
	public IPopulation<ADemoEntity, Attribute<? extends IValue>> getSolution() {
		return population;
	}
	
	// ------------------------ //
	
	private AFullNDimensionalMatrix<Integer> makeSwitch(AFullNDimensionalMatrix<Integer> matrix, 
			Map<ADemoEntity, ADemoEntity> theSwitch){
		
		for(ADemoEntity oldEntity : theSwitch.keySet()) {
			matrix.getVal(matrix.getCoordinates(new HashSet<>(oldEntity.getValues()))
					.iterator().next()).add(-1);
			
			matrix.getVal(matrix.getCoordinates(new HashSet<>(theSwitch.get(oldEntity).getValues()))
					.iterator().next()).add(1);
		}
		
		return matrix;
	}

}
