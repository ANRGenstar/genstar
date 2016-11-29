package spin.objects;

import java.util.Map;
import java.util.Set;

import core.metamodel.IEntity;


/** Network composé de noeud et de lien
 * 
 */
public class SpinNetwork <E extends IEntity, N extends NetworkNode<E>, L extends NetworkLink> {

	// Représentation du réseau. Une map de noeud, associé a un set de lien. 
	Map<N, Set<L>> networkRepresentation;
	
	// Methode de calcul quelconque
	

}
