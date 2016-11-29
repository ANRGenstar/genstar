package spin.algo.generator;

import spin.objects.SpinNetwork;
import core.metamodel.IAttribute;
import core.metamodel.IEntity;
import core.metamodel.IPopulation;
import core.metamodel.IValue;

/** Interface de générateur de réseau, commun a tous les générateurs. 
 * 
 *
 * @param <E> Extend IEntity<A,V> 
 * @param <L> Networklink, lien des réseaux
 * @param <N> NetworkNode<E>, node des réseaux, encapsule une entité
 * @param <V> IValue 
 * @param <A> IAttribute<V>, avec une valeur IValue
 */
public interface INetworkGenerator <V extends IValue, A extends IAttribute<V>>{
	/** Methode de création d'un réseau prenant une population générique et créant
	 * le réseau associé. 
	 * 
	 * @param population population en paramètre, implementant l'interface IPopulation 
	 * @return un SpinNetwork
	 */
	public SpinNetwork<V, A> generateNetwork(IPopulation<IEntity<A,V>, A, V> population);
	
}
