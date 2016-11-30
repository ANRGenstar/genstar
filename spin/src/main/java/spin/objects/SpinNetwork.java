package spin.objects;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import core.metamodel.IAttribute;
import core.metamodel.IValue;


/** Network composé de noeud et de lien
 * 
 */
public class SpinNetwork <V extends IValue, A extends IAttribute<V>> {

	// Représentation du réseau. Une map de noeud, associé a un set de lien. 
	public Map<NetworkNode<V,A>, Set<NetworkLink>> networkRepresentation;
	
	/** Constructeur sans param. 
	 * 
	 */
	public SpinNetwork(){
		networkRepresentation = new HashMap<NetworkNode<V,A>, Set<NetworkLink>>();
	}
	
	// Methode de calcul quelconque
	

}
