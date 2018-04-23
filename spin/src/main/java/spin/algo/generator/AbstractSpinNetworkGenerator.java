package spin.algo.generator;

import core.metamodel.IPopulation;
import core.metamodel.attribute.Attribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.value.IValue;
import gospl.GosplEntity;
import gospl.GosplPopulation;
import spin.SpinNetwork;

public abstract class AbstractSpinNetworkGenerator<E extends ADemoEntity> implements ISpinNetworkGenerator<E>{

	String networkName;
	
	public AbstractSpinNetworkGenerator(String _networkName) {
		this.networkName = _networkName;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public SpinNetwork generate(int numberOfIndividual) {
		GosplPopulation pop = new GosplPopulation();
		for(int i = 0 ; i<numberOfIndividual;i++) {
			pop.add(new GosplEntity());
		}
		return generate( (IPopulation<E, Attribute<? extends IValue>>) pop);
	}	
	
}
