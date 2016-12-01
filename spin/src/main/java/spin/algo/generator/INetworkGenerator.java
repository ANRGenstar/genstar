package spin.algo.generator;

import spin.objects.NetworkNode;
import spin.objects.SpinNetwork;
import gospl.metamodel.GosplEntity;
import gospl.metamodel.GosplPopulation;

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
	public abstract SpinNetwork generateNetwork(GosplPopulation population);
	
	public static SpinNetwork loadPopulation(GosplPopulation population){
	//Create a SpinNetwork with nodes linked to population entities
	//The SpinNetwork has all the needed nodes and no links
		SpinNetwork myNetwork = new SpinNetwork();
				
		// create all the nodes 
		for (GosplEntity entity : population) 
					myNetwork.putNode(new NetworkNode(entity));
		return myNetwork;
	}
	
}
