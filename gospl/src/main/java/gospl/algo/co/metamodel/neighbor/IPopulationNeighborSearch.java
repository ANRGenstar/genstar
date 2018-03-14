package gospl.algo.co.metamodel.neighbor;

import java.util.Collection;
import java.util.Map;

import core.metamodel.IPopulation;
import core.metamodel.attribute.Attribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.value.IValue;

/**
 * Express how to define neighborhood in the context of synthetic population: what are the possible
 * neighbors of a given synthetic population
 * 
 * @author kevinchapuis
 *
 * @param <U>
 */
public interface IPopulationNeighborSearch<U> {

	/**
	 * Find a neighbor population given any predicate to based neighborhood on and
	 * a degree (something close as moor or von neuman neighborhood notion). More
	 * precisely the predicate will define the properties to shift the population, e.g.
	 * an attribute (then the population next to a population will be a switch on that attribute);
	 * while the degree could be the number of changes on that properties (e.g. 1 individual or several)
	 * 
	 * @param population
	 * @param predicate
	 * @param degree
	 * @return
	 */
	public IPopulation<ADemoEntity, Attribute<? extends IValue>> getNeighbor(
			IPopulation<ADemoEntity, Attribute<? extends IValue>> population, 
			U predicate, int degree);
	
	/**
	 * Given a predicate property find two entities to be swap
	 * 
	 * @param population
	 * @param predicate
	 * @return
	 */
	public Map<ADemoEntity, ADemoEntity> findPairedTarget(
			IPopulation<ADemoEntity, Attribute<? extends IValue>> population, U predicate);
	
	/**
	 * The predicates to be used
	 * @return
	 */
	public Collection<U> getPredicates();
	
	/**
	 * Set the collection of predicate that could be used
	 */
	public void setPredicates(Collection<U> predicates);
	
	/**
	 * Add a new predicate
	 * @param predicate
	 */
	public void addPredicates(U predicate);
	
	/**
	 * The sample of entities which is the reservoir to swap entities from given population to its neighbors
	 * @param sample
	 */
	public void setSample(IPopulation<ADemoEntity, Attribute<? extends IValue>> sample);

	// ----------------- UTILITY ----------------- //
	
	/**
	 * Ensure that two entities have been deeply switch in the population
	 * <p>
	 * WARNING: newEntity should be a clone of an existing one, if one already exist in the population
	 * 
	 * @param population
	 * @param oldEntity
	 * @param newEntity
	 * @return
	 */
	public static IPopulation<ADemoEntity, Attribute<? extends IValue>> deepSwitch(
			IPopulation<ADemoEntity, Attribute<? extends IValue>> population, 
			ADemoEntity oldEntity, ADemoEntity newEntity){
		if(!population.remove(oldEntity) || !population.add(newEntity))
				throw new RuntimeException("Encounter a problem while switching between two entities:\n"
						+ "remove entity = "+oldEntity.toString()+"\n"
						+ "new entity = "+newEntity.toString());
		return population;
	}
	
}
