package spin.algo.generator;

import spin.objects.NetworkLink;
import spin.objects.NetworkNode;
import spin.objects.SpinNetwork;
import core.metamodel.IAttribute;
import core.metamodel.IEntity;
import core.metamodel.IPopulation;
import core.metamodel.IValue;

public class RandomNetworkGenerator<E extends IEntity<A,V>, N extends NetworkNode<E>, L extends NetworkLink,V extends IValue, A extends IAttribute<V>>
implements INetworkGenerator<E, L, N, V, A> {

	@Override
	
	/** generateur aléatoire
	 * 
	 */
	public SpinNetwork<E, N, L> generateNetwork(IPopulation<IEntity<A, V>, A, V> population) {
		
		
		// TODO Auto-generated method stub
		return null;
	}

	

}
