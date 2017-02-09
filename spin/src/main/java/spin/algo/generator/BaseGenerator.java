package spin.algo.generator;

import core.metamodel.IPopulation;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;
import spin.interfaces.INetworkGenerator;
import spin.objects.NetworkNode;
import spin.objects.SpinNetwork;

public abstract class BaseGenerator implements INetworkGenerator {
	
	
	public SpinNetwork loadPopulation(IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> population){
	//Create a SpinNetwork with nodes linked to population entities
	//The SpinNetwork has all the needed nodes and no links
		SpinNetwork myNetwork = new SpinNetwork();
		int i = 0;		
		
		// create all the nodes 
		for (APopulationEntity entity : population) 
					myNetwork.putNode(new NetworkNode(entity, i++));
		return myNetwork;
	}
}
