package spin.algo.generator;

import java.util.ArrayList;
import java.util.List;

import org.graphstream.graph.Node;

import spin.SpinPopulation;
import spin.objects.SpinNetwork;


public class RegularNetworkGenerator {

	/**
	 * 
	 * @param population
	 * @param k
	 * @return
	 */
	public SpinNetwork generateNetwork(SpinNetwork myNetwork, int k) {
		
		//int k connectivite
		//TODO: traiter le cas de conectivity pas paire ... 
		
		List<Node> nodes = new ArrayList<>(myNetwork.getNodes());
		
		// for each node i, create a link to i+1 ... i+k/2
		int link_id = 0;
		for (int i=0; i<nodes.size();i++){
			for (int j=1;j<=k/2;j++){
				myNetwork.putLink(String.valueOf(link_id), nodes.get(i), nodes.get((i+j)%nodes.size()));
				link_id++;				
			}
		}
		return myNetwork;
	}
	

}
