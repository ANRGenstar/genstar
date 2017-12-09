package spin.algo.generator;

import core.metamodel.IPopulation;
import core.metamodel.attribute.demographic.DemographicAttribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.value.IValue;
import gospl.GosplEntity;
import spin.SpinPopulation;

public interface ISpinPopulationGenerator<E extends ADemoEntity> {
	public SpinPopulation<E> generate(IPopulation<E, DemographicAttribute<? extends IValue>> myPopulation);
	public SpinPopulation<GosplEntity> generate(int numberOfIndividual);

}
