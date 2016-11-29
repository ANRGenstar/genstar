package spin.algo.generator;

import spin.objects.NetworkNode;
import spin.objects.SpinNetwork;
import core.metamodel.IAttribute;
import core.metamodel.IEntity;
import core.metamodel.IPopulation;
import core.metamodel.IValue;

public class RandomNetworkGenerator<V extends IValue, A extends IAttribute<V>>
//implements INetworkGenerator<E, L, N, V, A> 
{


	
	/** generateur aléatoire
	 * 
	 */
	public SpinNetwork<V,A> generateNetwork(IPopulation<IEntity<A,V>,A, V> population) {
		for (IEntity<A, V> entity : population) {
			NetworkNode<V,A> node = new NetworkNode<V, A>(entity);
		}
		
		
		return null;
	}

}
