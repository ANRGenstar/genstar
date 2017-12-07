package spin.algo.generator.network;

import core.metamodel.IPopulation;
import core.metamodel.attribute.demographic.DemographicAttribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.value.IValue;
import spin.SpinPopulation;

public interface INetworkGenerator<E extends ADemoEntity> {
	public SpinPopulation<E> generateNetwork(IPopulation<E, DemographicAttribute<? extends IValue>> myNetwork);
}
