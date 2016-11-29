package spin.objects;

import java.util.Set;

import core.metamodel.IEntity;

/** Noeud du réseau du réseau contenant un individu
 * 
 * @author Felix
 *
 * @param <E>
 */
public abstract class NetworkNode <E extends IEntity> {
	// Entity associated 
	E individuAssociated;
	
	// Connected node
	Set<NetworkNode<E>> connectedNodes;
	
	
}
