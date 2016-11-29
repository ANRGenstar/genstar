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

public class SWGenerator<V extends IValue, A extends IAttribute<V>> implements INetworkGenerator<V, A> 
{
	/** pour le moment algorithmie bidon.
	 * 
	 */
	@Override
	public SpinNetwork<V, A> generateNetwork(IPopulation<IEntity<A,V>, A, V> population) {
		// table temporaire pour garder une référence indexé sur les noeuds créés
		Hashtable<Integer, NetworkNode<V, A>> nodeCreated = new Hashtable<Integer, NetworkNode<V, A>>();
		int i = 0;
		
		// crée un objet spinNetwork
		SpinNetwork<V,A> myNetwork = new SpinNetwork<V,A>();
		
		// cyclé sur la pop
		for (IEntity<A,V> entity : population) {
			
			// créer un objet noeud et lui associer l'entité, puis ajouter le node a spinNetwork
			// on crée un objet noeud et un set de lien vide qui sera rempli apres
//			N node = new N(entity);
			
//			 new NetworkNode<E, V, A>(entity);
			NetworkNode<V, A> node =  new NetworkNode<V, A>(entity);
			nodeCreated.put(i++, node);
			myNetwork.networkRepresentation.put(node, new HashSet<NetworkLink>());
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
		
		for (NetworkNode<V,A> node : myNetwork.networkRepresentation.keySet()) {
			
			
			// créer un lien vers d'autre noeud puis ajouter les liens a spin ET mettre a jour la liste des noeuds connectés a un noeud
//			nodeCreated.get(0).add(nodeCreated.get(3));
		}
		//for
				
		// fin de création des liens
		
		
		return myNetwork;
	}

	
}



