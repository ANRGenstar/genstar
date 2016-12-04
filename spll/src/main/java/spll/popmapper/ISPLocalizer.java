package spll.popmapper;

import core.metamodel.IPopulation;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;

public interface ISPLocalizer {

	/**
	 * Provide the higher order method that take a population and 
	 * return the population with localisation indication 
	 * 
	 * @param population
	 * @return
	 */
	public IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> localisePopulation(
			IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> population);
	
}
