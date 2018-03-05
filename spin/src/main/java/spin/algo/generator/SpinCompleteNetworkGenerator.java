package spin.algo.generator;

import core.metamodel.IPopulation;
import core.metamodel.attribute.Attribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.value.IValue;
import spin.SpinNetwork;
import spin.SpinPopulation;
import spin.algo.factory.SpinNetworkFactory;

public class SpinCompleteNetworkGenerator<E extends ADemoEntity> extends  AbstractSpinPopulationGenerator<E> {

	@Override
	public SpinPopulation<E> generate(IPopulation<E, Attribute<? extends IValue>> myPop) {
		SpinNetwork network = SpinNetworkFactory.loadPopulation(myPop);
		
		// TODO: manage directed / undirected grph ...
		for(E n1 : myPop) {
			for(E n2 : myPop) {
				if(!n1.equals(n2)) {
					network.putLink(""+ n1.getEntityId() + " -> " + n2.getEntityId(), n1, n2);
				}
			}
		}
		
		return new SpinPopulation<>(myPop, network);
	}

}
