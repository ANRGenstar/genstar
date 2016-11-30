package spin.objects;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import core.metamodel.IAttribute;
import core.metamodel.IValue;


/** Network composé de noeud et de lien
 * 
 */
public class SpinNetwork <A extends IAttribute<V>,V extends IValue> {

	// Représentation du réseau. Une map de noeud, associé a un set de lien. 
	private Map<NetworkNode<A,V>, Set<NetworkLink>> networkRepresentation;
	
	/** Constructeur sans param. 
	 * 
	 */
	public SpinNetwork(){
		networkRepresentation = new HashMap<NetworkNode<A,V>, Set<NetworkLink>>();
	}
	
	/**
	 * Put a new NetworkNode in the graph. 
	 * An new set of NetworkLink is associated.
	 * @param node the NetworkNode to add
	 */
	public void putNode(NetworkNode<A, V> node) {
		networkRepresentation.put(node, new HashSet<NetworkLink>());		
	}

	public Set<NetworkNode<A, V>> getNodes() {
		return networkRepresentation.keySet();
	}
	
	
	// Methode de calcul quelconque
	

}
