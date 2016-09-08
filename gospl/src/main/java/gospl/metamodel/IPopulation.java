package gospl.metamodel;

import java.util.Collection;
import java.util.Set;

import gospl.metamodel.attribut.IAttribute;

/**
 * Describes a sample of a population, that is many individuals.
 * 
 * @author gospl-team
 *
 */
public interface IPopulation extends Collection<IEntity> {

	public Set<IAttribute> getPopulationAttributes();
	
	public String csvReport(CharSequence csvSep);
	
}
