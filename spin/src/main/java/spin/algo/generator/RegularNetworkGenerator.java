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

public class RegularNetworkGenerator<A extends IAttribute<V>,V extends IValue> implements INetworkGenerator<A, V>{

int k;	//connectivite par noeud ou nombre de lien par noeud
		//Il faut que k soit paire !!!
	
public SpinNetwork<A, V> generateNetwork(IPopulation<IEntity<A, V>, A, V> population) {
	return generateNetwork(population, 4);//4 Valeur par défaut si la connectivite n'est pas précisée
}	

public SpinNetwork<A, V> generateNetwork(IPopulation<IEntity<A, V>, A, V> population, int connectivity) {
		
		this.k = connectivity;
		//TODO: traiter le cas de conectivity pas paire ... 
		
		// List the created nodes
		List<NetworkNode<A, V>> nodesCreated = new ArrayList<NetworkNode<A, V>>();
		
		// create the spinNetwork
		SpinNetwork<A,V> myNetwork = new SpinNetwork<A,V>();
		
		// create all the nodes 
		for (IEntity<A,V> entity : population) {
			NetworkNode<A, V> node =  new NetworkNode<A, V>(entity);
			nodesCreated.add(node);
			myNetwork.putNode(node);
		}		
		
		// for each node i, create a link to i+1 ... i+k/2
		for (int i=0; i<nodesCreated.size();i++){
			
		}
		
		
		// create the links
		/**int i = 0;
		while (i < ndLink) {
			NetworkNode nodeOrigin = nodesCreated.get(rand.nextInt(nbNodes));
			NetworkNode nodeTarget = nodesCreated.get(rand.nextInt(nbNodes));
		
			// TODO : create links
			
		}*/

		
		
		
		
		return null;
	}
	

}
