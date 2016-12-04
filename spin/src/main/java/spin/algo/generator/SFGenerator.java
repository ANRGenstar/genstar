package spin.algo.generator;

import core.metamodel.IAttribute;
import core.metamodel.IPopulation;
import core.metamodel.IValue;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;
import spin.objects.SpinNetwork;

public class SFGenerator<A extends IAttribute<V>,V extends IValue> implements INetworkGenerator
{

	@Override
	public SpinNetwork generateNetwork(IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> population) {
		// TODO Auto-generated method stub
		return null;
	}

}
