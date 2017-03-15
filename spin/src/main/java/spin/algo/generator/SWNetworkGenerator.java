package spin.algo.generator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.graphstream.graph.*;

import core.metamodel.IPopulation;
import core.metamodel.pop.APopulationAttribute;
import core.metamodel.pop.APopulationEntity;
import core.metamodel.pop.APopulationValue;
import spin.SpinPopulation;
import spin.objects.SpinNetwork;
import useless.BaseGenerator;
import useless.NetworkLink;
import useless.NetworkNode;

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
	public SpinNetwork generateNetwork(SpinPopulation population, int k, double beta){
		//int k connectivity of the network
		//double beta noise introduced on the regular network
		// cree un reseau regulier 
		SpinNetwork myNetwork = (new RegularNetworkGenerator()).generateNetwork(population,k);
				
		//parcourir tous les liens
		HashSet<Edge> links = new HashSet<>(myNetwork.getLinks());
		List<Node> nodes = new ArrayList<>(myNetwork.getNodes());
		int nbNodes = nodes.size();
		
		//pour chacun si proba < beta ; supprimer (des deux cotes) et rebrancher aleatoirement 
		Random rand = new Random();
		for(Edge l : links){
			if(rand.nextDouble()<beta){
				myNetwork.removeLink(l);
				
				Node nodeFrom, nodeTo;
				boolean linkCreated=false;
				// create the links
				int link_id = 0;
				while (!linkCreated) {
					nodeFrom = nodes.get(rand.nextInt(nbNodes));
					nodeTo = nodes.get(rand.nextInt(nbNodes));
					
					if(!nodeFrom.equals(nodeTo)&&!nodeFrom.hasEdgeBetween(nodeTo)){
						myNetwork.putLink(String.valueOf(link_id), nodeFrom, nodeTo);
						linkCreated=true;
						link_id++;
						//nodeFrom.addLink(link);
						//nodeTo.addLink(link);
					}					
				}
			}
		}
		
		return myNetwork;
	}

	
}



