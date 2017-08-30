package spin.algo.generator;

import java.util.ArrayList;
import java.util.List;

import org.graphstream.graph.Node;

import spin.objects.SpinNetwork;


public class RegularNetworkGenerator {

	/** Génération d'un réseau régulier. 
	 * 
	 * @param myNetwork réseau de base
	 * @param k connectivité du réseau
	 * @return myNetwork réseau final
	 */
	public SpinNetwork generateNetwork(SpinNetwork myNetwork, int k) {
		
		//int k connectivite
		//TODO: traiter le cas de conectivity pas paire ... 
		
		List<Node> nodes = new ArrayList<>(myNetwork.getNodes());
		
		// for each node i, create a link to i+1 ... i+k/2
		int link_id = 0;
		for(int i=0; i<nodes.size();i++) {
			for (int j=1;j<=k/2;j++){
				myNetwork.putLink(String.valueOf(link_id), nodes.get(i), nodes.get((i+j)%nodes.size()));
				link_id++;				
			}
		}
		
		if(k%2 == 1 && nodes.size()%2 == 0) {
			for(int i=0 ; i<nodes.size() ; i++) {
				Node n1 = nodes.get(i);
				int j = (k/2)+1;
				while(n1.getDegree()<k) {
					Node n2 = nodes.get((i+j)%nodes.size());
					if(n2.getDegree()<k && !n1.hasEdgeBetween(n2)) {
						myNetwork.putLink(String.valueOf(link_id), n1, n2);
						link_id++;
					}
					j++;
				}
			}
		}
		
		return myNetwork;
	}
	

}
