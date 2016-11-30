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

public class RandomNetworkGenerator<V extends IValue, A extends IAttribute<V>> implements INetworkGenerator<V, A> 
//implements INetworkGenerator<E, L, N, V, A> 
{
	
	double proba;
	
	/** generateur aléatoire
	 * 
	 */
	public SpinNetwork<V,A> generateNetwork(IPopulation<IEntity<A,V>,A, V> population) {
		// TODO: check random generator 
		Random rand = new Random();
		
		// List the created nodes
		List<NetworkNode<V, A>> nodesCreated = new ArrayList<NetworkNode<V, A>>();
		int nbNodes;
		
		// create the spinNetwork
		SpinNetwork<V,A> myNetwork = new SpinNetwork<V,A>();

		// create all the nodes 
		for (IEntity<A,V> entity : population) {
			// créer un objet noeud et lui associer l'entité, puis ajouter le node a spinNetwork
			// on crée un objet noeud et un set de lien vide qui sera rempli apres
//			N node = new N(entity);
			
//			 new NetworkNode<E, V, A>(entity);
			NetworkNode<V, A> node =  new NetworkNode<V, A>(entity);
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
			NetworkNode<V,A> node = new NetworkNode<V, A>(entity);
		}
		
		
		return null;
	}

}
