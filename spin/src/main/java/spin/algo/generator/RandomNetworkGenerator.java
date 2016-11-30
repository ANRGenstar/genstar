package spin.algo.generator;

import spin.objects.NetworkLink;
import spin.objects.NetworkNode;
import spin.objects.SpinNetwork;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import core.metamodel.IAttribute;
import core.metamodel.IEntity;
import core.metamodel.IPopulation;
import core.metamodel.IValue;

public class RandomNetworkGenerator<A extends IAttribute<V>,V extends IValue> implements INetworkGenerator<A, V> 
//implements INetworkGenerator<E, L, N, V, A> 
{
	
	double proba;
	
	/** generateur aléatoire
	 * 
	 */
	public SpinNetwork<A,V> generateNetwork(IPopulation<IEntity<A,V>,A, V> population) {
		// TODO: check random generator 
		Random rand = new Random();
		
		// List the created nodes
		List<NetworkNode<A, V>> nodesCreated = new ArrayList<NetworkNode<A, V>>();
		int nbNodes;
		
		// create the spinNetwork
		SpinNetwork<A,V> myNetwork = new SpinNetwork<A,V>();

		// create all the nodes 
		for (IEntity<A,V> entity : population) {
			// créer un objet noeud et lui associer l'entité, puis ajouter le node a spinNetwork
			// on crée un objet noeud et un set de lien vide qui sera rempli apres
//			N node = new N(entity);
			
//			 new NetworkNode<E, V, A>(entity);
			NetworkNode<A,V> node =  new NetworkNode<A,V>(entity);
			nodesCreated.add(node);
			myNetwork.putNode(node);
		}		
		nbNodes = nodesCreated.size();
		
		// Compute the number of links to generate
		// TODO: revoir le type de réseau à générer (diriger ou non ?) 
		int ndLink = (int) Math.round(population.size()*population.size()*proba);
		
		// create the links
		int i = 0;
		while (i < ndLink) {
			NetworkNode nodeOrigin = nodesCreated.get(rand.nextInt(nbNodes));
			NetworkNode nodeTarget = nodesCreated.get(rand.nextInt(nbNodes));
		
			// TODO : create links
			
		}

		
		// old 
		
		for (IEntity<A, V> entity : population) {
			NetworkNode<A,V> node = new NetworkNode<A,V>(entity);
		}
		
		
		return null;
	}

}
