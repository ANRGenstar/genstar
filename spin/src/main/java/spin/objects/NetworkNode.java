package spin.objects;

import core.metamodel.IEntity;

/** Noeud du réseau du réseau contenant un individu
 * 
 * @author Felix
 *
 * @param <E>
 */
public abstract class NetworkNode <E extends IEntity> {
	E monIndividu;
}
