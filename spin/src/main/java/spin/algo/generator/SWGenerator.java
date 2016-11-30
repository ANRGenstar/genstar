package spin.algo.generator;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Random;

import spin.objects.NetworkLink;
import spin.objects.NetworkNode;
import spin.objects.SpinNetwork;
import core.metamodel.IAttribute;
import core.metamodel.IEntity;
import core.metamodel.IPopulation;
import core.metamodel.IValue;

public class SWGenerator<A extends IAttribute<V>,V extends IValue> implements INetworkGenerator<A, V> 
{
	/** pour le moment algorithmie bidon.
	 * 
	 */
	@Override
	public SpinNetwork<A, V> generateNetwork(IPopulation<IEntity<A,V>, A, V> population) {
		// table temporaire pour garder une référence indexé sur les noeuds créés
		Hashtable<Integer, NetworkNode<A,V>> nodeCreated = new Hashtable<Integer, NetworkNode<A, V>>();
		int i = 0;
		
		// crée un objet spinNetwork
		SpinNetwork<A,V> myNetwork = new SpinNetwork<A,V>();
		
		// cyclé sur la pop
		for (IEntity<A,V> entity : population) {
			
			// créer un objet noeud et lui associer l'entité, puis ajouter le node a spinNetwork
			// on crée un objet noeud et un set de lien vide qui sera rempli apres
//			N node = new N(entity);
			
//			 new NetworkNode<E, V, A>(entity);
			NetworkNode<A,V> node =  new NetworkNode<A,V>(entity);
			nodeCreated.put(i++, node);
			myNetwork.putNode(node);
		}
		
		
		
		// fin de création de tous les noeuds du réseau 
		
		// Cycle sur les noeuds du réseau
		for (int j = 0; j < nodeCreated.size(); j++) {
			int indexDestination = j;
			while (indexDestination == j)
				indexDestination = new Random().nextInt(nodeCreated.size() - 1);
			if(nodeCreated.get(j).
					connectedNodes.
					contains(
							nodeCreated.get(indexDestination))){
				// Rien
			}
			else {
			nodeCreated.get(j).connectedNodes.add(nodeCreated.get(indexDestination));
			nodeCreated.get(indexDestination).connectedNodes.add(nodeCreated.get(j));
			}
		}
		
		for (NetworkNode<A,V> node : myNetwork.getNodes()) {
			
			
			// créer un lien vers d'autre noeud puis ajouter les liens a spin ET mettre a jour la liste des noeuds connectés a un noeud
//			nodeCreated.get(0).add(nodeCreated.get(3));
		}
		//for
				
		// fin de création des liens
		
		
		return myNetwork;
	}

	
}



