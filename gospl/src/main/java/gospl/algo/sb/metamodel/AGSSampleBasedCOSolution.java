package gospl.algo.sb.metamodel;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import core.metamodel.IPopulation;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;
import gospl.GosplPopulation;
import gospl.algo.sb.tabusearch.solution.GSUniqueShiftSolution;
import gospl.distribution.GosplNDimensionalMatrixFactory;
import gospl.distribution.matrix.AFullNDimensionalMatrix;
import gospl.validation.GosplIndicatorFactory;

/**
 * WARNING: huge performance issue with {@link #getTabuValue(Set)} because of transposed population
 * to contingency table (with possible population of several million of entity)
 * 
 * TODO: have a 'proxy' solution from current state solution, i.e. only store differences, that is
 * entity which have been remove and added 
 * 
 * @author kevinchapuis
 *
 */
public abstract class AGSSampleBasedCOSolution implements IGSSampleBasedCOSolution {

	protected IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> population;
	protected Set<APopulationValue> valueList;
	
	protected Collection<APopulationEntity> sample;
	
	private double fitness = -1;
	
	public AGSSampleBasedCOSolution(IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> population,
			Collection<APopulationEntity> sample){
		this.population = population;
		this.sample = sample;
		this.valueList = population.stream().flatMap(entity -> entity.getValues().stream())
				.collect(Collectors.toSet());
	}
	
	public AGSSampleBasedCOSolution(Collection<APopulationEntity> population, Collection<APopulationEntity> sample){
		this(new GosplPopulation(population), sample);
	}
	
	@Override
	public Double getFitness(Set<AFullNDimensionalMatrix<Integer>> objectives) {
		// Only compute once
		if(fitness == -1){
			AFullNDimensionalMatrix<Integer> popMatrix = GosplNDimensionalMatrixFactory
					.getFactory().createContingency(population);
			fitness = objectives.stream().mapToDouble(obj -> GosplIndicatorFactory.getFactory()
					.getIntegerTAE(obj, popMatrix)).sum();
		}
		return fitness;
	}
	
	@Override
	public IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> getSolution() {
		return population;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((population == null) ? 0 : population.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GSUniqueShiftSolution other = (GSUniqueShiftSolution) obj;
		if (population == null) {
			if (other.population != null)
				return false;
		} else if (!population.equals(other.population))
			return false;
		return true;
	}
	
}
