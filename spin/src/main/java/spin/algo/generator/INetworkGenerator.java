package spin.algo.generator;

import spin.objects.NetworkLink;
import spin.objects.NetworkNode;
import spin.objects.SpinNetwork;
import core.io.survey.attribut.ASurveyAttribute;
import core.io.survey.attribut.value.AValue;
import core.metamodel.IEntity;
import core.metamodel.IPopulation;

/** Interface de générateur de réseau, commun a tous les générateurs.
 * 
 *
 */
public interface INetworkGenerator <E extends IEntity, L extends NetworkLink, N extends NetworkNode<E>>{
	/** Methode de création d'un réseau prenant une population générique et créant
	 * le réseau associé. 
	 * 
	 * @param population population en paramètre, implementant l'interface IPopulation 
	 * @return un SpinNetwork
	 */
	public SpinNetwork<E, N, L> generateNetwork(IPopulation<IEntity<ASurveyAttribute, AValue>, ASurveyAttribute, AValue> population);
	
}
