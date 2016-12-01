package spll.popmapper;

import core.io.survey.entity.AGenstarEntity;
import core.io.survey.entity.attribut.AGenstarAttribute;
import core.io.survey.entity.attribut.value.AGenstarValue;
import core.metamodel.IPopulation;

public interface ISPLocalizer {

	/**
	 * Provide the higher order method that take a population and 
	 * return the population with localisation indication 
	 * 
	 * @param population
	 * @return
	 */
	public IPopulation<AGenstarEntity, AGenstarAttribute, AGenstarValue> localisePopulation(
			IPopulation<AGenstarEntity, AGenstarAttribute, AGenstarValue> population);
	
}
