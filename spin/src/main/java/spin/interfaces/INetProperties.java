package spin.interfaces;

import java.util.Set;

import core.metamodel.entity.ADemoEntity;

/** 
 * Interface pour obtenir des valeurs de propriete sur le graphe
 * genere sur la population. 
 *
 */
public interface INetProperties<E extends ADemoEntity> {
	
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
	double getClustering(E entite);
	
	/** Obtient les noeuds voisins a l'entite specifier
	 * 
	 * @param entite
	 * @return
	 */
	Set<E> getNeighboor(ADemoEntity entite);
	
	/** Obtient la densite du reseau
	 * 
	 * @return
	 */
	double getDensity();
	
}
