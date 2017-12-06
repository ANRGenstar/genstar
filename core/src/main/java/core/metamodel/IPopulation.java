package core.metamodel;

import java.util.Collection;
import java.util.Set;

import core.metamodel.attribute.IAttribute;
import core.metamodel.entity.IEntity;
import core.metamodel.value.IValue;

/**
 * A population is a collection of entity characterized by a set of attribute.
 * Each attribute is made of one or more values, and a particular entity should have
 * a precise value for each.
 * <p>
 * More formally, an {@link IPopulation} is a {@link Collection} of {@link IEntity}
 * characterized by the same set of {@link IAttribute} and specified by a
 * corresponding set of {@link IValue}; i.e. each attribute is binded to a value 
 * for a particular entity
 * 
 * @author gospl-team
 *
 */
public interface IPopulation<E extends IEntity<A>, A extends IAttribute<? extends IValue>> extends Collection<E> {

	/**
	 * Returns the set of the attributs which exist in this population
	 * @return
	 */
	public Set<A> getPopulationAttributes();
	
	/**
	 * returns the population attribute for the given name of attribute,
	 * or null if it does not exists
	 * @param attribute
	 * @return
	 */
	public A getPopulationAttributeNamed(String name);
	
	/**
	 * returns true if all the entities are of type type.
	 * @param type
	 * @return
	 */
	public boolean isAllPopulationOfType(String type);
	
}
