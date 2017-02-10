package spin.algo.generator;

import java.util.ArrayList;
import java.util.List;

import core.metamodel.IPopulation;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;
import spin.objects.NetworkLink;
import spin.objects.NetworkNode;
import spin.objects.SpinNetwork;


public class RegularNetworkGenerator extends BaseGenerator{

	/**
	 * 
	 * @param population
	 * @param k
	 * @return
	 */
	public SpinNetwork generateNetwork(IPopulation<APopulationEntity, APopulationAttribute, APopulationValue> population, int k) {
		
		//int k connectivité
		//TODO: traiter le cas de conectivity pas paire ... 
		
		// create the spinNetwork
		SpinNetwork myNetwork = loadPopulation(population);
		List<NetworkNode> nodes = new ArrayList<>(myNetwork.getNodes());
		
		// for each node i, create a link to i+1 ... i+k/2
		int link_id = 0;
		for (int i=0; i<nodes.size();i++){
			for (int j=1;j<=k/2;j++){
				NetworkLink l=new NetworkLink(nodes.get(i),nodes.get((i+j)%nodes.size()),false,String.valueOf(link_id));
				link_id++;
				nodes.get(i).addLink(l);
				nodes.get((i+j)%nodes.size()).addLink(l);				
			}
		}
		return myNetwork;
	}
	

}
