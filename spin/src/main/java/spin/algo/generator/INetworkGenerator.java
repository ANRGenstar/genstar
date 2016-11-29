package spin.algo.generator;

import spin.objects.NetworkLink;
import spin.objects.NetworkNode;
import spin.objects.SpinNetwork;
import core.io.survey.attribut.ASurveyAttribute;
import core.io.survey.attribut.value.AValue;
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
public interface INetworkGenerator <E extends IEntity<A,V>, L extends NetworkLink, N extends NetworkNode<E>,V extends IValue, A extends IAttribute<V> >{
	/** Methode de création d'un réseau prenant une population générique et créant
	 * le réseau associé. 
	 * 
	 * @param population population en paramètre, implementant l'interface IPopulation 
	 * @return un SpinNetwork
	 */
	public SpinNetwork<E, N, L> generateNetwork(IPopulation<IEntity<A, V>, A, V> population);
	
}
