package io.github.gospl.metamodel;

import java.util.Collection;

/**
 * Describes a sample of a population, that is many individuals.
 * 
 * @author gospl-team
 *
 */
public interface IPopulation {

	public void add(IEntity entity);
	public void remove(IEntity entity);
	public Collection<IEntity> getAll();
	
}
