package spin.objects;

import java.util.HashSet;
import java.util.Set;

import core.metamodel.IAttribute;
import core.metamodel.IEntity;
import core.metamodel.IValue;

/** Noeud du réseau du réseau contenant un individu
 * 
 * @author Felix
 *
 * @param <E>
 */
public class NetworkNode <V extends IValue, A extends IAttribute<V>> {
	// Entity associated 
	public IEntity<A,V> individuAssociated;
	
	// Connected node
	public Set<NetworkNode<V, A> > connectedNodes;
	
//	public NetworkNode(IEntity<A,V> entite){
//		individuAssociated = entite;
//		connectedNodes = new HashSet<NetworkNode<E, V, A>>();
//	}
	
	public NetworkNode(IEntity<A,V> entite){
		individuAssociated = entite;
		connectedNodes = new HashSet<NetworkNode<V, A>>();
	}
	
	
	
	
}

