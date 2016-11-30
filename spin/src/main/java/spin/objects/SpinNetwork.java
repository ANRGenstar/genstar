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
public class SpinNetwork <V extends IValue, A extends IAttribute<V>> {

	// Représentation du réseau. Une map de noeud, associé a un set de lien. 
	private Map<NetworkNode<V,A>, Set<NetworkLink>> networkRepresentation;
	
	/** Constructeur sans param. 
	 * 
	 */
	public SpinNetwork(){
		networkRepresentation = new HashMap<NetworkNode<V,A>, Set<NetworkLink>>();
	}
	
	/**
	 * Put a new NetworkNode in the graph. 
	 * An new set of NetworkLink is associated.
	 * @param node the NetworkNode to add
	 */
	public void putNode(NetworkNode<V, A> node) {
		networkRepresentation.put(node, new HashSet<NetworkLink>());		
	}

	public Set<NetworkNode<V, A>> getNodes() {
		return networkRepresentation.keySet();
	}
	
	
	// Methode de calcul quelconque
	

}
