package core.metamodel;

import java.util.Collection;
import java.util.Set;

/**
 * Describes a sample of a population, that is many individuals.
 * 
 * @author gospl-team
 *
 */
public interface IPopulation<E extends IEntity<A, V>, A extends IAttribute<V>, V extends IValue> extends Collection<E> {

	public Set<A> getPopulationAttributes();
	
	public String csvReport(CharSequence csvSep);
	
}
