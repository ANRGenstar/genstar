package spin.objects;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import core.metamodel.IAttribute;
import core.metamodel.IValue;


/** Network composé de noeud et de lien
 * 
 */
public class SpinNetwork {

	// Représentation du réseau. Une map de noeud, associé a un set de lien. 
	private Map<NetworkNode, Set<NetworkLink>> network;
	
	/** Constructeur sans param. 
	 * 
	 */
	public SpinNetwork(){
		network = new HashMap<NetworkNode, Set<NetworkLink>>();
	}
	
	/**
	 * Put a new NetworkNode in the graph. 
	 * An new set of NetworkLink is associated.
	 * @param node the NetworkNode to add
	 */
	public void putNode(NetworkNode node) {
		network.put(node, new HashSet<NetworkLink>());		
	}

	public Set<NetworkNode> getNodes() {
		return network.keySet();
	}
	
	public Set<NetworkLink> getLinks(){
		HashSet<NetworkNode> nodes = new HashSet(this.getNodes());
		HashSet<NetworkLink> links = new HashSet();
		for (NetworkNode n : nodes){
			for (NetworkLink l : n.getLinks()){
				if (!links.contains(l)){
					links.add(l);
				}
			}
		}
		return links;
	}
	
	
	
	// Methode de calcul quelconque
	

}
