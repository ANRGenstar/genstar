package spin.interfaces;

import java.util.Set;

import core.metamodel.pop.APopulationEntity;

/** 
 * Interface pour obtenir des valeurs de propriété sur le graphe
 * généré sur la population. 
 *
 */
public interface ISpinNetProperties {

	double getAPL();
	double getClustering(APopulationEntity entite);
	Set<APopulationEntity> getNeighboor(APopulationEntity entite);
	
}
