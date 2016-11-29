package spin.algo.generator;

import core.metamodel.IAttribute;
import core.metamodel.IEntity;
import core.metamodel.IPopulation;
import core.metamodel.IValue;
import spin.objects.NetworkLink;
import spin.objects.NetworkNode;
import spin.objects.SpinNetwork;

public class SFGenerator<E extends IEntity<A,V>, L extends NetworkLink, N extends NetworkNode<E>,V extends IValue, A extends IAttribute<V> > implements INetworkGenerator<E,L,N,V,A> {

	@Override
	public SpinNetwork<E, N, L> generateNetwork(IPopulation<IEntity<A, V>, A, V> population) {
		// TODO Auto-generated method stub
		return null;
	}



}
