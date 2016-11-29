package spin.algo.generator;

import spin.objects.SpinNetwork;
import core.metamodel.IAttribute;
import core.metamodel.IEntity;
import core.metamodel.IPopulation;
import core.metamodel.IValue;

public class SFGenerator<V extends IValue, A extends IAttribute<V> > implements INetworkGenerator<V,A> 
{

	@Override
	public SpinNetwork<V, A> generateNetwork(IPopulation<IEntity<A, V>, A, V> population) {
		// TODO Auto-generated method stub
		return null;
	}



}
