package spin.algo.generator.network;

import core.metamodel.IPopulation;
import core.metamodel.attribute.demographic.DemographicAttribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.value.IValue;
import spin.SpinNetwork;
import spin.SpinPopulation;
import spin.algo.factory.SpinNetworkFactory;

public class CompleteNetworkGenerator<E extends ADemoEntity> implements INetworkGenerator<E> {

	@Override
	public SpinPopulation<E> generateNetwork(IPopulation<E, DemographicAttribute<? extends IValue>> myPop) {
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
