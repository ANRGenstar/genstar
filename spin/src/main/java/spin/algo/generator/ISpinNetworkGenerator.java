package spin.algo.generator;

import core.metamodel.IPopulation;
import core.metamodel.attribute.Attribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.value.IValue;
import spin.SpinNetwork;

public interface ISpinNetworkGenerator<E extends ADemoEntity> {
	public SpinNetwork generate(IPopulation<E, Attribute<? extends IValue>> myPopulation);
	public SpinNetwork generate(int numberOfIndividual);

}
