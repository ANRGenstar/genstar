package spin.algo.generator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

import spin.objects.SpinNetwork;

/**Générateur SmallWorld
 * 
 *
 */
public class SWNetworkGenerator
{
	/**
	 * 
	 * @param population
	 * @param k
	 * @param beta
	 * @return
	 */
	public SpinNetwork generateNetwork(SpinNetwork network, int k, double beta){
		//int k connectivity of the network
		//double beta noise introduced on the regular network
		// cree un reseau regulier 
		SpinNetwork myNetwork = (new RegularNetworkGenerator()).generateNetwork(network,k);
				
		//parcourir tous les liens
		HashSet<Edge> links = new HashSet<>(myNetwork.getLinks());
		int nbLinks = links.size();
		List<Node> nodes = new ArrayList<>(myNetwork.getNodes());
		int nbNodes = nodes.size();
		
		//pour chacun si proba < beta ; supprimer (des deux cotes) et rebrancher aleatoirement 
		Random rand = new Random();
		
		int link_id = nbLinks;
		for(Edge l : links){
			if(rand.nextDouble()<beta){
				myNetwork.removeLink(l);
				
				// create the links
				Node nodeFrom, nodeTo;
				boolean linkCreated=false;
				
				while (!linkCreated) {
					nodeFrom = nodes.get(rand.nextInt(nbNodes));
					nodeTo = nodes.get(rand.nextInt(nbNodes));
					
					if(!nodeFrom.equals(nodeTo)&&!nodeFrom.hasEdgeBetween(nodeTo)){
						myNetwork.putLink(String.valueOf(link_id), nodeFrom, nodeTo);
						linkCreated=true;
						link_id++;
					}					
				}
			}
		}
		
		return myNetwork;
	}

	
}



