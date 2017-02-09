package spin.interfaces;

import core.metamodel.IPopulation;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;
import spin.objects.NetworkNode;
import spin.objects.SpinNetwork;

/** Interface de générateur de réseau, commun a tous les générateurs. 
 * 
 *
 * @param <E> Extend IEntity<A,V> 
 * @param <L> Networklink, lien des réseaux
 * @param <N> NetworkNode<E>, node des réseaux, encapsule une entité
 * @param <V> IValue 
 * @param <A> IAttribute<V>, avec une valeur IValue
 */
public interface INetworkGenerator {
	/** Methode de création d'un réseau prenant une population générique et créant
	 * le réseau associé. 
	 * 
	 * @param population population en paramètre, implementant l'interface IPopulation 
	 * @return un SpinNetwork
	 */
	public abstract SpinNetwork generateNetwork(IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> population);
	
	
	public static SpinNetwork loadPopulation(IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> population){
	//Create a SpinNetwork with nodes linked to population entities
	//The SpinNetwork has all the needed nodes and no links
		SpinNetwork myNetwork = new SpinNetwork();
				
		// create all the nodes 
		for (APopulationEntity entity : population) 
					myNetwork.putNode(new NetworkNode(entity));
		return myNetwork;
	}
	
}
