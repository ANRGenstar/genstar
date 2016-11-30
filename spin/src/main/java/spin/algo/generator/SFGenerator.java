package spin.algo.generator;

import spin.objects.SpinNetwork;
import core.metamodel.IAttribute;
import core.metamodel.IEntity;
import core.metamodel.IPopulation;
import core.metamodel.IValue;

public class SFGenerator<A extends IAttribute<V>,V extends IValue> implements INetworkGenerator<A,V> 
{

	@Override
	public SpinNetwork<A, V> generateNetwork(IPopulation<IEntity<A, V>, A, V> population) {
		// TODO Auto-generated method stub
		return null;
	}



}
