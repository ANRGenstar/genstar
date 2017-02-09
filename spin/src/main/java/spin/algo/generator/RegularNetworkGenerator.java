package spin.algo.generator;

import java.util.ArrayList;
import java.util.List;

import core.metamodel.IPopulation;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;
import spin.interfaces.INetworkGenerator;
import spin.objects.NetworkLink;
import spin.objects.NetworkNode;
import spin.objects.SpinNetwork;


public class RegularNetworkGenerator implements INetworkGenerator{

	
public SpinNetwork generateNetwork(IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> population) {
	return generateNetwork(population, 4);//4 Valeur par défaut si la connectivite n'est pas précisée
}	

public SpinNetwork generateNetwork(IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> population, int k) {
		
		//int k connectivité
		//TODO: traiter le cas de conectivity pas paire ... 
		
		// create the spinNetwork
		SpinNetwork myNetwork = INetworkGenerator.loadPopulation(population);
		List<NetworkNode> nodes = new ArrayList<>(myNetwork.getNodes());
		
		// for each node i, create a link to i+1 ... i+k/2
		for (int i=0; i<nodes.size();i++){
			for (int j=1;j<=k/2;j++){
				NetworkLink l=new NetworkLink(nodes.get(i),nodes.get((i+j)%nodes.size()),false);
				nodes.get(i).addLink(l);
				nodes.get((i+j)%nodes.size()).addLink(l);				
			}
		}
		return myNetwork;
	}
	

}
