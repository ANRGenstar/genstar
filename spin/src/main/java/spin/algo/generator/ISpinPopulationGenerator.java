package spin.algo.generator;

import core.metamodel.IPopulation;
import core.metamodel.attribute.Attribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.value.IValue;
import gospl.GosplEntity;
import spin.SpinPopulation;

public interface ISpinPopulationGenerator<E extends ADemoEntity> {
	public SpinPopulation<E> generate(IPopulation<E, Attribute<? extends IValue>> myPopulation);
	public SpinPopulation<GosplEntity> generate(int numberOfIndividual);

}
