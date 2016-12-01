package spin.algo.generator;

import core.io.survey.entity.AGenstarEntity;
import core.io.survey.entity.attribut.AGenstarAttribute;
import core.io.survey.entity.attribut.value.AGenstarValue;
import core.metamodel.IAttribute;
import core.metamodel.IPopulation;
import core.metamodel.IValue;
import spin.objects.SpinNetwork;

public class SFGenerator<A extends IAttribute<V>,V extends IValue> implements INetworkGenerator
{

	@Override
	public SpinNetwork generateNetwork(IPopulation<AGenstarEntity, AGenstarAttribute, AGenstarValue> population) {
		// TODO Auto-generated method stub
		return null;
	}

}
