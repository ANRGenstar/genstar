package spll.popmatcher;

import core.metamodel.IAttribute;
import core.metamodel.IEntity;
import core.metamodel.IPopulation;
import core.metamodel.IValue;

public interface ISpllPopulationMatcher {

	/**
	 * Provide the higher order method that take a population and 
	 * return the population with localisation indication 
	 * 
	 * @param population
	 * @return
	 */
	public IPopulation<IEntity<IAttribute<IValue>, IValue>, IAttribute<IValue>, IValue> localisePopulation();
	
}
