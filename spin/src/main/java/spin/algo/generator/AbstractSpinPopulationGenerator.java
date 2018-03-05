package spin.algo.generator;

import core.metamodel.IPopulation;
import core.metamodel.attribute.Attribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.value.IValue;
import gospl.GosplEntity;
import gospl.GosplPopulation;
import spin.SpinPopulation;

public abstract class AbstractSpinPopulationGenerator<E extends ADemoEntity> implements ISpinPopulationGenerator<E>{

	@SuppressWarnings("unchecked")
	@Override
	public SpinPopulation<GosplEntity> generate(int numberOfIndividual) {
		GosplPopulation pop = new GosplPopulation();
		for(int i = 0 ; i<numberOfIndividual;i++) {
			pop.add(new GosplEntity());
		}
		return (SpinPopulation<GosplEntity>) generate( (IPopulation<E, Attribute<? extends IValue>>) pop);
	}	
	
}
