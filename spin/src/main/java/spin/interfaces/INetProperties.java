package spin.interfaces;

import java.util.Set;

import core.metamodel.pop.APopulationEntity;

/** 
 * Interface pour obtenir des valeurs de propriété sur le graphe
 * généré sur la population. 
 *
 */
public interface INetProperties {

	/** obtention du plus court chemin moyen sur le graphe. 
	 * 
	 * @return la valeur de plus court chemin moyen sur le graphe
	 */
	double getAPL();
	
	/** Obtient le clustering autour d'un noeud en particulier
	 * 
	 * @param entite
	 * @return
	 */
	double getClustering(APopulationEntity entite);
	
	/** Obtient les noeuds voisins a l'entite spécifier
	 * 
	 * @param entite
	 * @return
	 */
	Set<APopulationEntity> getNeighboor(APopulationEntity entite);
	
	/** Obtient la densité du réseau
	 * 
	 * @return
	 */
	double getDensity();
	
}
